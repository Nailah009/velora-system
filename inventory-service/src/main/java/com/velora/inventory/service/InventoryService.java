package com.velora.inventory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.inventory.dto.ProductRequest;
import com.velora.inventory.dto.ReserveItemRequest;
import com.velora.inventory.dto.ReserveRequest;
import com.velora.inventory.dto.ReserveResponse;
import com.velora.inventory.event.InventoryReservationEvent;
import com.velora.inventory.model.ProductVariant;
import com.velora.inventory.repository.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final ProductVariantRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.inventory-reserved}")
    private String inventoryReservedTopic;

    @Value("${app.kafka.topics.inventory-failed}")
    private String inventoryFailedTopic;

    public InventoryService(ProductVariantRepository repository,
                            KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public List<ProductVariant> getAllProducts() {
        return repository.findAll();
    }

    public ProductVariant getVariantBySku(String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("SKU not found: " + sku));
    }

    @Transactional
    public ProductVariant createProduct(ProductRequest request) {
        if (repository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }

        ProductVariant variant = new ProductVariant();
        apply(variant, request);
        ProductVariant saved = repository.save(variant);

        System.out.println("""
        ============================================================
        INVENTORY SERVICE - PRODUCT CREATED
        ============================================================
        SKU       : %s
        Product   : %s
        Category  : %s
        Color/Size: %s / %s
        Price     : %s
        Stock     : %s
        ============================================================
        """.formatted(
                saved.getSku(), saved.getProductName(), saved.getCategory(), saved.getColor(),
                saved.getSize(), saved.getPrice(), saved.getStock()
        ));

        return saved;
    }

    @Transactional
    public ProductVariant updateProduct(Long id, ProductRequest request) {
        ProductVariant variant = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        apply(variant, request);
        return repository.save(variant);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Product not found");
        }
        repository.deleteById(id);
    }

    /**
     * Endpoint REST lama tetap dipertahankan agar tidak merusak Swagger/API lama.
     * Namun untuk Saga utama, reserve stock sekarang dipanggil oleh Kafka consumer
     * melalui reserveStockFromOrderEvent().
     */
    @Transactional
    public ReserveResponse reserveStock(ReserveRequest request) {
        System.out.println("""
        ============================================================
        INVENTORY SERVICE - START RESERVE STOCK VIA REST
        ============================================================
        Items : %s
        Note  : REST fallback/manual test. Main Saga uses Kafka consumer.
        ============================================================
        """.formatted(formatItems(request.getItems())));

        ReserveResponse response = new ReserveResponse();
        BigDecimal total = BigDecimal.ZERO;

        for (ReserveRequestItem item : request.getItems().stream().map(ReserveRequestItem::new).toList()) {
            ProductVariant variant = getVariantBySku(item.sku());

            if (variant.getStock() < item.quantity()) {
                throw new IllegalArgumentException("Insufficient stock for SKU " + item.sku());
            }

            variant.setStock(variant.getStock() - item.quantity());
            repository.save(variant);

            BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(item.quantity()));
            total = total.add(subtotal);
            response.getItems().add(new ReserveResponse.ReservedItemResponse(
                    variant.getSku(), variant.getProductName(), item.quantity(), variant.getPrice(), subtotal
            ));
        }

        response.setTotalAmount(total);
        return response;
    }

    /**
     * Core fix: reserve inventory via Kafka, mengikuti pola Saga Choreography repo dosen.
     */
    @Transactional
    public void reserveStockFromOrderEvent(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        Long orderId = event.get("orderId").asLong();

        try {
            JsonNode items = event.get("items");
            if (items == null || !items.isArray() || items.isEmpty()) {
                throw new IllegalArgumentException("Order event does not contain item list");
            }

            System.out.println("""
            ============================================================
            INVENTORY SERVICE - START RESERVE STOCK VIA KAFKA
            ============================================================
            Saga ID        : %s
            Order ID       : %s
            Items          : %s
            Correlation ID : %s
            ============================================================
            """.formatted(
                    text(event, "sagaId"),
                    orderId,
                    formatEventItems(items),
                    text(event, "correlationId")
            ));

            Map<String, ProductVariant> variants = new LinkedHashMap<>();

            // Validate all stock first. This avoids partial reserve if one SKU fails.
            for (JsonNode item : items) {
                String sku = item.get("sku").asText();
                int quantity = item.get("quantity").asInt();
                ProductVariant variant = getVariantBySku(sku);
                variants.put(sku, variant);

                System.out.println("""
                ------------------------------------------------------------
                INVENTORY SERVICE - CHECK STOCK
                ------------------------------------------------------------
                SKU           : %s
                Product       : %s
                Available     : %s
                Requested Qty : %s
                ------------------------------------------------------------
                """.formatted(variant.getSku(), variant.getProductName(), variant.getStock(), quantity));

                if (variant.getStock() < quantity) {
                    throw new IllegalArgumentException("Insufficient stock for SKU " + sku + ". Available=" + variant.getStock() + ", requested=" + quantity);
                }
            }

            BigDecimal total = BigDecimal.ZERO;
            List<InventoryReservationEvent.Item> reservedItems = new ArrayList<>();

            for (JsonNode item : items) {
                String sku = item.get("sku").asText();
                int quantity = item.get("quantity").asInt();
                ProductVariant variant = variants.get(sku);

                int before = variant.getStock();
                variant.setStock(variant.getStock() - quantity);
                repository.save(variant);

                BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(quantity));
                total = total.add(subtotal);

                InventoryReservationEvent.Item reservedItem = new InventoryReservationEvent.Item();
                reservedItem.sku = variant.getSku();
                reservedItem.productName = variant.getProductName();
                reservedItem.quantity = quantity;
                reservedItem.price = variant.getPrice();
                reservedItem.subtotal = subtotal;
                reservedItems.add(reservedItem);

                System.out.println("""
                ------------------------------------------------------------
                INVENTORY SERVICE - STOCK RESERVED
                ------------------------------------------------------------
                SKU          : %s
                Quantity     : %s
                Stock Before : %s
                Stock After  : %s
                Subtotal     : %s
                ------------------------------------------------------------
                """.formatted(sku, quantity, before, variant.getStock(), subtotal));
            }

            InventoryReservationEvent out = baseEvent(event);
            out.totalAmount = total;
            out.status = "RESERVED";
            out.reason = "Inventory reserved successfully";
            out.items = reservedItems;

            kafkaTemplate.send(inventoryReservedTopic, orderId.toString(), objectMapper.writeValueAsString(out));

            System.out.println("""
            ============================================================
            INVENTORY SERVICE - INVENTORY RESERVED EVENT PUBLISHED
            ============================================================
            Topic          : %s
            Saga ID        : %s
            Order ID       : %s
            Total Amount   : %s
            Status         : %s
            Next Step      : PROCESS_PAYMENT
            Correlation ID : %s
            ============================================================
            """.formatted(
                    inventoryReservedTopic,
                    out.sagaId,
                    out.orderId,
                    out.totalAmount,
                    out.status,
                    out.correlationId
            ));

        } catch (Exception ex) {
            publishInventoryFailed(event, ex.getMessage());
        }
    }

    private void publishInventoryFailed(JsonNode event, String reason) throws Exception {
        Long orderId = event.get("orderId").asLong();

        InventoryReservationEvent out = baseEvent(event);
        out.totalAmount = BigDecimal.ZERO;
        out.status = "FAILED";
        out.reason = reason;
        out.items = new ArrayList<>();

        kafkaTemplate.send(inventoryFailedTopic, orderId.toString(), objectMapper.writeValueAsString(out));

        System.out.println("""
        ============================================================
        INVENTORY SERVICE - INVENTORY FAILED EVENT PUBLISHED
        ============================================================
        Topic          : %s
        Saga ID        : %s
        Order ID       : %s
        Status         : %s
        Reason         : %s
        Next Step      : CANCEL_ORDER
        Correlation ID : %s
        ============================================================
        """.formatted(
                inventoryFailedTopic,
                out.sagaId,
                out.orderId,
                out.status,
                out.reason,
                out.correlationId
        ));
    }

    private InventoryReservationEvent baseEvent(JsonNode event) {
        InventoryReservationEvent out = new InventoryReservationEvent();
        out.eventId = UUID.randomUUID().toString();
        out.occurredAt = Instant.now();
        out.correlationId = text(event, "correlationId");
        out.sagaId = text(event, "sagaId");
        out.orderId = event.get("orderId").asLong();
        out.userId = event.has("userId") && !event.get("userId").isNull() ? event.get("userId").asLong() : null;
        out.username = text(event, "username");
        out.email = text(event, "email");
        out.paymentMethod = text(event, "paymentMethod");
        out.shippingAddress = text(event, "shippingAddress");
        return out;
    }

    @Transactional
    public void releaseStockFromSagaEvent(JsonNode event, String compensationReason) {
        System.out.println("""
        ============================================================
        INVENTORY SERVICE - SAGA COMPENSATION STARTED
        ============================================================
        Saga ID  : %s
        Order ID : %s
        Action   : RELEASE_RESERVED_STOCK
        Reason   : %s
        ============================================================
        """.formatted(
                event.has("sagaId") ? event.get("sagaId").asText() : "-",
                event.has("orderId") ? event.get("orderId").asText() : "-",
                compensationReason
        ));

        JsonNode items = event.get("items");
        if (items == null || !items.isArray()) {
            System.out.println("""
            ============================================================
            INVENTORY SERVICE - COMPENSATION SKIPPED
            ============================================================
            Reason : Event does not contain item list
            ============================================================
            """);
            return;
        }

        for (JsonNode item : items) {
            String sku = item.get("sku").asText();
            int quantity = item.get("quantity").asInt();
            ProductVariant variant = getVariantBySku(sku);

            int before = variant.getStock();
            variant.setStock(variant.getStock() + quantity);
            repository.save(variant);

            System.out.println("""
            ------------------------------------------------------------
            INVENTORY SERVICE - STOCK RELEASED
            ------------------------------------------------------------
            SKU          : %s
            Quantity     : %s
            Stock Before : %s
            Stock After  : %s
            ------------------------------------------------------------
            """.formatted(sku, quantity, before, variant.getStock()));
        }

        System.out.println("""
        ============================================================
        INVENTORY SERVICE - SAGA COMPENSATION COMPLETED
        ============================================================
        Saga ID      : %s
        Order ID     : %s
        Final Action : STOCK_RELEASED
        ============================================================
        """.formatted(
                event.has("sagaId") ? event.get("sagaId").asText() : "-",
                event.has("orderId") ? event.get("orderId").asText() : "-"
        ));
    }

    private void apply(ProductVariant variant, ProductRequest request) {
        variant.setSku(request.getSku());
        variant.setProductName(request.getProductName());
        variant.setCategory(request.getCategory());
        variant.setColor(request.getColor());
        variant.setSize(request.getSize());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());
    }

    private String formatItems(List<ReserveItemRequest> items) {
        return items.stream()
                .map(item -> item.getSku() + " x" + item.getQuantity())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String formatEventItems(JsonNode items) {
        List<String> result = new ArrayList<>();
        for (JsonNode item : items) {
            result.add(item.get("sku").asText() + " x" + item.get("quantity").asInt());
        }
        return result.stream().collect(Collectors.joining(", ", "[", "]"));
    }

    private String text(JsonNode event, String field) {
        return event.has(field) && !event.get(field).isNull() ? event.get(field).asText() : "-";
    }

    private record ReserveRequestItem(String sku, Integer quantity) {
        ReserveRequestItem(ReserveItemRequest item) {
            this(item.getSku(), item.getQuantity());
        }
    }
}
