package com.velora.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.order.dto.CreateOrderRequest;
import com.velora.order.dto.OrderItemRequest;
import com.velora.order.event.OrderCreatedEvent;
import com.velora.order.model.Order;
import com.velora.order.model.OrderItem;
import com.velora.order.model.OrderStatus;
import com.velora.order.repository.OrderRepository;
import com.velora.order.security.JwtUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.order-created}")
    private String orderTopic;

    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<String, String> kafkaTemplate,
                        ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * FULL SAGA CHOREOGRAPHY VERSION.
     *
     * Sebelumnya: order-service REST langsung ke inventory-service untuk reserve stock.
     * Sekarang: order-service hanya membuat order lalu publish OrderCreatedEvent.
     * Inventory-service yang consume event tersebut dan melakukan reserve stock via Kafka.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request, JwtUser user, String authorizationHeader) throws Exception {
        System.out.println("""
        ============================================================
        ORDER SERVICE - START CREATE ORDER
        ============================================================
        Customer       : %s
        Email          : %s
        Address        : %s
        Payment Method : %s
        Items          : %s
        Integration    : Kafka Saga Choreography
        ============================================================
        """.formatted(
                user.getUsername(),
                user.getEmail(),
                request.getShippingAddress(),
                request.getPaymentMethod(),
                formatRequestItems(request.getItems())
        ));

        String correlationId = UUID.randomUUID().toString();

        Order order = new Order();
        order.setUserId(user.getUserId());
        order.setUsername(user.getUsername());
        order.setEmail(user.getEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.PENDING_INVENTORY);
        order.setCorrelationId(correlationId);

        for (OrderItemRequest requestItem : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setSku(requestItem.getSku());
            item.setProductName(requestItem.getSku());
            item.setQuantity(requestItem.getQuantity());
            item.setPrice(BigDecimal.ZERO);
            item.setSubtotal(BigDecimal.ZERO);
            order.addItem(item);

            System.out.println("""
            ------------------------------------------------------------
            ORDER SERVICE - ORDER ITEM ACCEPTED
            ------------------------------------------------------------
            SKU      : %s
            Quantity : %s
            Note     : Price/subtotal will be filled after inventory reserved event
            ------------------------------------------------------------
            """.formatted(item.getSku(), item.getQuantity()));
        }

        Order saved = orderRepository.save(order);

        System.out.println("""
        ============================================================
        ORDER SERVICE - ORDER SAVED
        ============================================================
        Order ID       : %s
        Order Code     : %s
        Saga ID        : %s
        Customer       : %s
        Total Amount   : %s
        Status         : %s
        Correlation ID : %s
        Created At     : %s
        ============================================================
        """.formatted(
                saved.getId(),
                formatOrderCode(saved),
                formatSagaId(saved),
                saved.getUsername(),
                saved.getTotalAmount(),
                saved.getStatus(),
                saved.getCorrelationId(),
                saved.getCreatedAt()
        ));

        System.out.println("""
        ============================================================
        SAGA - ORDER FULFILLMENT STARTED
        ============================================================
        Saga ID   : %s
        Order ID  : %s
        Order Code: %s
        Step      : CREATE_ORDER
        Status    : SUCCESS
        Next Step : RESERVE_STOCK_BY_INVENTORY_SERVICE
        Broker    : Kafka topic %s
        ============================================================
        """.formatted(formatSagaId(saved), saved.getId(), formatOrderCode(saved), orderTopic));

        publishOrderCreated(saved);
        return saved;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public void markInventoryReserved(JsonNode event) {
        Long orderId = event.get("orderId").asLong();
        Order order = getOrder(orderId);

        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setTotalAmount(event.get("totalAmount").decimalValue());

        if (event.has("items") && event.get("items").isArray()) {
            order.getItems().clear();
            for (JsonNode node : event.get("items")) {
                OrderItem item = new OrderItem();
                item.setSku(node.get("sku").asText());
                item.setProductName(node.get("productName").asText());
                item.setQuantity(node.get("quantity").asInt());
                item.setPrice(node.get("price").decimalValue());
                item.setSubtotal(node.get("subtotal").decimalValue());
                order.addItem(item);
            }
        }

        orderRepository.save(order);
        printSagaStep(
                "SAGA - INVENTORY STEP COMPLETED",
                order,
                "RESERVE_STOCK",
                "SUCCESS",
                "PROCESS_PAYMENT"
        );
    }

    @Transactional
    public void markInventoryFailed(Long orderId, String reason) {
        Order order = getOrder(orderId);
        order.setStatus(OrderStatus.INVENTORY_FAILED);
        orderRepository.save(order);

        printSagaStep(
                "SAGA - ORDER CANCELLED BY INVENTORY FAILED",
                order,
                "RESERVE_STOCK",
                "FAILED",
                "CANCEL_ORDER + SEND_NOTIFICATION"
        );

        System.out.println("""
        ============================================================
        ORDER SERVICE - INVENTORY FAILED REASON
        ============================================================
        Order ID : %s
        Reason   : %s
        ============================================================
        """.formatted(orderId, reason));
    }

    @Transactional
    public void markPaymentSuccess(Long orderId) {
        Order order = getOrder(orderId);
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        printSagaStep("SAGA - PAYMENT STEP COMPLETED", order, "PROCESS_PAYMENT", "SUCCESS", "CREATE_SHIPMENT");
    }

    @Transactional
    public void markPaymentFailed(Long orderId) {
        Order order = getOrder(orderId);
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
        printSagaStep("SAGA - COMPENSATION TRIGGERED BY PAYMENT FAILED", order, "PROCESS_PAYMENT", "FAILED", "RELEASE_STOCK + SEND_NOTIFICATION");
    }

    @Transactional
    public void markReadyToShip(Long orderId) {
        Order order = getOrder(orderId);
        order.setStatus(OrderStatus.READY_TO_SHIP);
        orderRepository.save(order);
        printSagaStep("SAGA - ORDER FULFILLMENT COMPLETED", order, "CREATE_SHIPMENT", "SUCCESS", "SEND_NOTIFICATION");
    }

    @Transactional
    public void markShipmentFailed(Long orderId) {
        Order order = getOrder(orderId);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        printSagaStep("SAGA - COMPENSATION TRIGGERED BY SHIPMENT FAILED", order, "CREATE_SHIPMENT", "FAILED", "REFUND_PAYMENT + RELEASE_STOCK + CANCEL_ORDER");
    }

    private void publishOrderCreated(Order order) throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.eventId = UUID.randomUUID().toString();
        event.occurredAt = Instant.now();
        event.correlationId = order.getCorrelationId();
        event.sagaId = formatSagaId(order);
        event.orderId = order.getId();
        event.userId = order.getUserId();
        event.username = order.getUsername();
        event.email = order.getEmail();
        event.totalAmount = order.getTotalAmount();
        event.paymentMethod = order.getPaymentMethod();
        event.shippingAddress = order.getShippingAddress();
        event.items = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            OrderCreatedEvent.Item eItem = new OrderCreatedEvent.Item();
            eItem.sku = item.getSku();
            eItem.productName = item.getProductName();
            eItem.quantity = item.getQuantity();
            eItem.price = item.getPrice();
            eItem.subtotal = item.getSubtotal();
            event.items.add(eItem);
        }

        kafkaTemplate.send(orderTopic, order.getId().toString(), objectMapper.writeValueAsString(event));

        System.out.println("""
        ============================================================
        ORDER SERVICE - ORDER CREATED EVENT PUBLISHED
        ============================================================
        Topic          : %s
        Order ID       : %s
        Saga ID        : %s
        Customer       : %s
        Email          : %s
        Payment Method : %s
        Status         : %s
        Correlation ID : %s
        Next Consumer  : inventory-service
        ============================================================
        """.formatted(
                orderTopic,
                order.getId(),
                formatSagaId(order),
                order.getUsername(),
                order.getEmail(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getCorrelationId()
        ));
    }

    private String formatOrderCode(Order order) {
        int year = order.getCreatedAt() == null
                ? Year.now().getValue()
                : order.getCreatedAt().atZone(ZoneId.systemDefault()).getYear();
        return "ORD-" + year + "-" + String.format("%04d", order.getId());
    }

    private String formatSagaId(Order order) {
        int year = order.getCreatedAt() == null
                ? Year.now().getValue()
                : order.getCreatedAt().atZone(ZoneId.systemDefault()).getYear();
        return "SAGA-" + year + "-" + String.format("%04d", order.getId());
    }

    private void printSagaStep(String title, Order order, String step, String status, String nextStep) {
        System.out.println("""
        ============================================================
        %s
        ============================================================
        Saga ID        : %s
        Order ID       : %s
        Order Code     : %s
        Step           : %s
        Status         : %s
        Next Step      : %s
        Current Order  : %s
        Correlation ID : %s
        ============================================================
        """.formatted(
                title,
                formatSagaId(order),
                order.getId(),
                formatOrderCode(order),
                step,
                status,
                nextStep,
                order.getStatus(),
                order.getCorrelationId()
        ));
    }

    private String formatRequestItems(List<OrderItemRequest> items) {
        return items.stream()
                .map(item -> item.getSku() + " x" + item.getQuantity())
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
