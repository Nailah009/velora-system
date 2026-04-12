package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryReserveResponse;
import com.example.inventoryservice.dto.ProductVariantResponse;
import com.example.inventoryservice.dto.ReserveInventoryRequest;
import com.example.inventoryservice.dto.ReserveItemRequest;
import com.example.inventoryservice.dto.ProductCatalogResponse;
import com.example.inventoryservice.dto.ProductCatalogVariantResponse;
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
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
        List<Product> products = productRepository.findAll();

        return products.stream().map(product -> {
            List<ProductVariant> variants = productVariantRepository.findByProductIdOrderBySkuAsc(product.getId());

            BigDecimal startingPrice = variants.stream()
                    .map(ProductVariant::getPrice)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            BigDecimal highestPrice = variants.stream()
                    .map(ProductVariant::getPrice)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            int totalAvailableStock = variants.stream()
                    .map(ProductVariant::getAvailableStock)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            List<String> colors = variants.stream()
                    .map(ProductVariant::getColorName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            List<String> sizes = variants.stream()
                    .map(ProductVariant::getSizeName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            List<ProductCatalogVariantResponse> variantResponses = variants.stream().map(variant -> {
                ProductCatalogVariantResponse item = new ProductCatalogVariantResponse();
                item.setSku(variant.getSku());
                item.setColorName(variant.getColorName());
                item.setSizeName(variant.getSizeName());
                item.setPrice(variant.getPrice());
                item.setAvailableStock(variant.getAvailableStock());
                item.setReservedStock(variant.getReservedStock());
                return item;
            }).toList();

            ProductCatalogResponse response = new ProductCatalogResponse();
            response.setId(product.getId());
            response.setName(product.getName());
            response.setBrand(product.getBrand());
            response.setCategory(product.getCategory());
            response.setStartingPrice(startingPrice);
            response.setHighestPrice(highestPrice);
            response.setTotalAvailableStock(totalAvailableStock);
            response.setAvailableColors(colors);
            response.setAvailableSizes(sizes);
            response.setVariants(variantResponses);

            return response;
        }).toList();
    }

    public List<ProductVariant> getVariantsByProduct(String productId) {
        return productVariantRepository.findByProductIdOrderBySkuAsc(productId);
    }

    public ProductVariantResponse getVariantResponse(String sku) {
        ProductVariant variant = productVariantRepository.findById(sku)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + sku));

        Product product = productRepository.findById(variant.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found for SKU: " + sku));

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
            int newReserved = currentReserved - item.getQuantity();
            if (newReserved < 0) {
                throw new IllegalStateException("Reserved stock became negative for SKU: " + item.getSku());
            }

            variant.setReservedStock(newReserved);
            variant.setAvailableStock(variant.getAvailableStock() + item.getQuantity());
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
