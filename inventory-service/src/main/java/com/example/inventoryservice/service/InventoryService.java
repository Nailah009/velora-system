package com.example.inventoryservice.service;

    import com.example.inventoryservice.dto.InventoryReserveResponse;
    import com.example.inventoryservice.dto.ProductCatalogResponse;
    import com.example.inventoryservice.dto.ProductVariantResponse;
    import com.example.inventoryservice.dto.ReserveInventoryRequest;
    import com.example.inventoryservice.dto.ReserveItemRequest;
    import com.example.inventoryservice.entity.Product;
    import com.example.inventoryservice.entity.ProductVariant;
    import com.example.inventoryservice.event.OrderItemPayload;
    import com.example.inventoryservice.event.PaymentResultEvent;
    import com.example.inventoryservice.repository.ProductRepository;
    import com.example.inventoryservice.repository.ProductVariantRepository;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;

    @Service
    public class InventoryService {

        private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

        private final ProductRepository productRepository;
        private final ProductVariantRepository productVariantRepository;

        public InventoryService(ProductRepository productRepository, ProductVariantRepository productVariantRepository) {
            this.productRepository = productRepository;
            this.productVariantRepository = productVariantRepository;
        }

        public List<ProductCatalogResponse> getAllProducts() {
            return productVariantRepository.findAllWithProduct().stream().map(variant -> {
                Product product = variant.getProduct();

                ProductCatalogResponse response = new ProductCatalogResponse();
                response.setId(product.getId());
                response.setSku(variant.getSku());
                response.setName(product.getName());
                response.setBrand(product.getBrand());
                response.setCategory(product.getCategory());
                response.setColorName(variant.getColorName());
                response.setSizeName(variant.getSizeName());
                response.setPrice(variant.getPrice());
                response.setAvailableStock(variant.getAvailableStock());
                response.setReservedStock(variant.getReservedStock());
                return response;
            }).toList();
        }

        public List<ProductVariant> getVariantsByProduct(String productId) {
            return productVariantRepository.findByProduct_IdOrderBySkuAsc(productId);
        }

        public ProductVariantResponse getVariantResponse(String sku) {
            ProductVariant variant = productVariantRepository.findById(sku)
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + sku));

            Product product = variant.getProduct();
            if (product == null) {
                product = productRepository.findById(variant.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found for SKU: " + sku));
            }

            ProductVariantResponse response = new ProductVariantResponse();
            response.setSku(variant.getSku());
            response.setProductId(product.getId());
            response.setProductName(product.getName());
            response.setBrand(product.getBrand());
            response.setCategory(product.getCategory());
            response.setColorName(variant.getColorName());
            response.setSizeName(variant.getSizeName());
            response.setPrice(variant.getPrice());
            response.setAvailableStock(variant.getAvailableStock());
            response.setReservedStock(variant.getReservedStock());
            return response;
        }

        @Transactional
        public InventoryReserveResponse reserveStock(ReserveInventoryRequest request) {
            log.info("""
============================================================
INVENTORY SERVICE - START RESERVE STOCK
============================================================
Order ID      : {}
Items         : {}
============================================================
""",
                    request.getOrderId(),
                    request.getItems().stream().map(i -> i.getSku() + " x" + i.getQuantity()).toList());

            for (ReserveItemRequest item : request.getItems()) {
                ProductVariant variant = productVariantRepository.findById(item.getSku())
                        .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + item.getSku()));

                log.info("""
------------------------------------------------------------
INVENTORY SERVICE - CHECK VARIANT
------------------------------------------------------------
SKU           : {}
Available     : {}
Reserved      : {}
Requested Qty : {}
------------------------------------------------------------
""",
                        variant.getSku(),
                        variant.getAvailableStock(),
                        variant.getReservedStock(),
                        item.getQuantity());

                if (variant.getAvailableStock() < item.getQuantity()) {
                    throw new IllegalStateException("Stock is not enough for SKU: " + item.getSku());
                }

                variant.setAvailableStock(variant.getAvailableStock() - item.getQuantity());
                variant.setReservedStock(variant.getReservedStock() + item.getQuantity());
                productVariantRepository.save(variant);

                log.info("""
------------------------------------------------------------
INVENTORY SERVICE - STOCK RESERVED
------------------------------------------------------------
SKU           : {}
New Available : {}
New Reserved  : {}
------------------------------------------------------------
""",
                        variant.getSku(),
                        variant.getAvailableStock(),
                        variant.getReservedStock());
            }

            return new InventoryReserveResponse(request.getOrderId(), "RESERVED", request.getItems());
        }

        @Transactional
        public void confirmStock(PaymentResultEvent event) {
            log.info("""
============================================================
INVENTORY SERVICE - CONFIRM STOCK AFTER PAYMENT SUCCESS
============================================================
Order ID      : {}
Customer      : {}
Items         : {}
============================================================
""",
                    event.getOrderId(),
                    event.getCustomerName(),
                    event.getItems().stream().map(i -> i.getSku() + " x" + i.getQuantity()).toList());

            for (OrderItemPayload item : event.getItems()) {
                ProductVariant variant = productVariantRepository.findById(item.getSku())
                        .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + item.getSku()));

                int currentReserved = variant.getReservedStock() == null ? 0 : variant.getReservedStock();
                int newReserved = currentReserved - item.getQuantity();
                if (newReserved < 0) {
                    throw new IllegalStateException("Reserved stock became negative for SKU: " + item.getSku());
                }

                variant.setReservedStock(newReserved);
                productVariantRepository.save(variant);

                log.info("""
------------------------------------------------------------
INVENTORY SERVICE - STOCK CONFIRMED
------------------------------------------------------------
SKU           : {}
Available     : {}
Reserved      : {}
------------------------------------------------------------
""",
                        variant.getSku(),
                        variant.getAvailableStock(),
                        variant.getReservedStock());
            }
        }

        @Transactional
        public void releaseStock(PaymentResultEvent event) {
            log.info("""
============================================================
INVENTORY SERVICE - RELEASE STOCK AFTER PAYMENT FAILED
============================================================
Order ID      : {}
Reason        : {}
Items         : {}
============================================================
""",
                    event.getOrderId(),
                    event.getReason(),
                    event.getItems().stream().map(i -> i.getSku() + " x" + i.getQuantity()).toList());

            for (OrderItemPayload item : event.getItems()) {
                ProductVariant variant = productVariantRepository.findById(item.getSku())
                        .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + item.getSku()));

                int currentReserved = variant.getReservedStock() == null ? 0 : variant.getReservedStock();
                int currentAvailable = variant.getAvailableStock() == null ? 0 : variant.getAvailableStock();
                int newReserved = currentReserved - item.getQuantity();
                if (newReserved < 0) {
                    throw new IllegalStateException("Reserved stock became negative for SKU: " + item.getSku());
                }

                variant.setReservedStock(newReserved);
                variant.setAvailableStock(currentAvailable + item.getQuantity());
                productVariantRepository.save(variant);

                log.info("""
------------------------------------------------------------
INVENTORY SERVICE - STOCK RELEASED
------------------------------------------------------------
SKU           : {}
Available     : {}
Reserved      : {}
------------------------------------------------------------
""",
                        variant.getSku(),
                        variant.getAvailableStock(),
                        variant.getReservedStock());
            }
        }
    }
