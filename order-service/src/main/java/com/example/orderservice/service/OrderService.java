package com.example.orderservice.service;

import com.example.orderservice.dto.*;
import com.example.orderservice.entity.CustomerOrder;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.event.OrderItemPayload;
import com.example.orderservice.event.PaymentResultEvent;
import com.example.orderservice.event.ShipmentCreatedEvent;
import com.example.orderservice.producer.OrderEventProducer;
import com.example.orderservice.repository.CustomerOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final CustomerOrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final RestTemplate restTemplate;

    @Value("${service.inventory.base-url}")
    private String inventoryBaseUrl;

    public OrderService(CustomerOrderRepository orderRepository,
                        OrderEventProducer orderEventProducer,
                        RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("""
============================================================
ORDER SERVICE - START CREATE ORDER
============================================================
Customer      : {}
Email         : {}
Address       : {}
Payment Method: {}
Items         : {}
============================================================
""",
                request.getCustomerName(),
                request.getEmail(),
                request.getShippingAddress(),
                request.getPaymentMethod(),
                request.getItems().stream().map(i -> i.getSku() + " x" + i.getQuantity()).toList());

        String orderId = UUID.randomUUID().toString();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemPayload> eventItems = new ArrayList<>();
        List<InventoryReserveItemRequest> reserveItems = new ArrayList<>();
        List<String> summaries = new ArrayList<>();

        for (OrderItemRequest item : request.getItems()) {
            ProductVariantResponse variant = restTemplate.getForObject(
                    inventoryBaseUrl + "/api/variants/" + item.getSku(),
                    ProductVariantResponse.class);

            if (variant == null) {
                throw new IllegalArgumentException("Variant not found: " + item.getSku());
            }

            log.info("""
------------------------------------------------------------
ORDER SERVICE - PRODUCT VARIANT VALIDATED
------------------------------------------------------------
SKU           : {}
Product       : {}
Color / Size  : {} / {}
Price         : {}
Available     : {}
------------------------------------------------------------
""",
                    variant.getSku(),
                    variant.getProductName(),
                    variant.getColorName(),
                    variant.getSizeName(),
                    variant.getPrice(),
                    variant.getAvailableStock());

            totalAmount = totalAmount.add(variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            OrderItemPayload payload = new OrderItemPayload();
            payload.setSku(item.getSku());
            payload.setQuantity(item.getQuantity());
            payload.setUnitPrice(variant.getPrice());
            eventItems.add(payload);

            InventoryReserveItemRequest reserveItem = new InventoryReserveItemRequest();
            reserveItem.setSku(item.getSku());
            reserveItem.setQuantity(item.getQuantity());
            reserveItems.add(reserveItem);

            summaries.add(item.getSku() + " x" + item.getQuantity());
        }

        InventoryReserveRequest reserveRequest = new InventoryReserveRequest();
        reserveRequest.setOrderId(orderId);
        reserveRequest.setItems(reserveItems);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryReserveRequest> httpEntity = new HttpEntity<>(reserveRequest, headers);

        log.info("""
============================================================
ORDER SERVICE - REQUEST INVENTORY RESERVE
============================================================
Order ID      : {}
Inventory URL : {}{}
============================================================
""",
                orderId,
                inventoryBaseUrl,
                "/api/inventory/reserve");

        restTemplate.postForEntity(inventoryBaseUrl + "/api/inventory/reserve", httpEntity, String.class);

        log.info("""
============================================================
ORDER SERVICE - INVENTORY RESERVE SUCCESS
============================================================
Order ID      : {}
Items         : {}
============================================================
""",
                orderId,
                summaries);

        CustomerOrder order = new CustomerOrder();
        order.setId(orderId);
        order.setCustomerName(request.getCustomerName());
        order.setEmail(request.getEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setItemsSummary(String.join(", ", summaries));
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("""
============================================================
ORDER SERVICE - ORDER SAVED
============================================================
Order ID      : {}
Customer      : {}
Total Amount  : {}
Status        : {}
Created At    : {}
============================================================
""",
                order.getId(),
                order.getCustomerName(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt());

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(orderId);
        event.setCustomerName(request.getCustomerName());
        event.setEmail(request.getEmail());
        event.setShippingAddress(request.getShippingAddress());
        event.setItems(eventItems);
        event.setTotalAmount(totalAmount);
        event.setPaymentMethod(request.getPaymentMethod());
        orderEventProducer.sendOrderCreated(event);

        log.info("""
============================================================
ORDER SERVICE - WAITING PAYMENT RESULT
============================================================
Order ID      : {}
Current Status: {}
============================================================
""",
                orderId,
                order.getStatus());

        return new OrderResponse(order.getId(), order.getStatus().name(), order.getTotalAmount());
    }

    public Optional<CustomerOrder> findById(String orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional
    public void updateFromPaymentResult(PaymentResultEvent event) {
        CustomerOrder order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + event.getOrderId()));

        OrderStatus oldStatus = order.getStatus();
        if ("SUCCESS".equalsIgnoreCase(event.getPaymentStatus())) {
            order.setStatus(OrderStatus.PAID);
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }
        orderRepository.save(order);

        log.info("""
============================================================
ORDER SERVICE - UPDATE STATUS FROM PAYMENT RESULT
============================================================
Order ID      : {}
Customer      : {}
Payment Status: {}
Reason        : {}
Old Status    : {}
New Status    : {}
============================================================
""",
                event.getOrderId(),
                order.getCustomerName(),
                event.getPaymentStatus(),
                event.getReason(),
                oldStatus,
                order.getStatus());
    }

    @Transactional
    public void updateFromShipmentCreated(ShipmentCreatedEvent event) {
        CustomerOrder order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + event.getOrderId()));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.READY_TO_SHIP);
        orderRepository.save(order);

        log.info("""
============================================================
ORDER SERVICE - UPDATE STATUS FROM SHIPMENT RESULT
============================================================
Order ID      : {}
Tracking No   : {}
Courier       : {}
Old Status    : {}
New Status    : {}
============================================================
""",
                event.getOrderId(),
                event.getTrackingNumber(),
                event.getCourier(),
                oldStatus,
                order.getStatus());
    }
}
