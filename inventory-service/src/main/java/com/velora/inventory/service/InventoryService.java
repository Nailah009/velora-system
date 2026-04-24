package com.velora.inventory.service;

import com.velora.inventory.dto.ProductRequest;
import com.velora.inventory.dto.ReserveItemRequest;
import com.velora.inventory.dto.ReserveRequest;
import com.velora.inventory.dto.ReserveResponse;
import com.velora.inventory.model.ProductVariant;
import com.velora.inventory.repository.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    private final ProductVariantRepository repository;

    public InventoryService(ProductVariantRepository repository) {
        this.repository = repository;
    }

    public List<ProductVariant> getAllProducts() {
        return repository.findAll();
    }

    public ProductVariant getVariantBySku(String sku) {
        return repository.findBySku(sku).orElseThrow(() -> new EntityNotFoundException("SKU not found: " + sku));
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
                """.formatted(saved.getSku(), saved.getProductName(), saved.getCategory(), saved.getColor(), saved.getSize(), saved.getPrice(), saved.getStock()));
        return saved;
    }

    @Transactional
    public ProductVariant updateProduct(Long id, ProductRequest request) {
        ProductVariant variant = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        apply(variant, request);
        return repository.save(variant);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!repository.existsById(id)) throw new EntityNotFoundException("Product not found");
        repository.deleteById(id);
    }

    @Transactional
    public ReserveResponse reserveStock(ReserveRequest request) {
        System.out.println("""
                
                ============================================================
                INVENTORY SERVICE - START RESERVE STOCK
                ============================================================
                Items          : %s
                ============================================================
                """.formatted(formatItems(request.getItems())));

        ReserveResponse response = new ReserveResponse();
        BigDecimal total = BigDecimal.ZERO;
        for (ReserveRequestItem item : request.getItems().stream().map(ReserveRequestItem::new).toList()) {
            ProductVariant variant = getVariantBySku(item.sku());

            System.out.println("""
                    ------------------------------------------------------------
                    INVENTORY SERVICE - CHECK VARIANT
                    ------------------------------------------------------------
                    SKU           : %s
                    Product       : %s
                    Available     : %s
                    Requested Qty : %s
                    ------------------------------------------------------------
                    """.formatted(variant.getSku(), variant.getProductName(), variant.getStock(), item.quantity()));

            if (variant.getStock() < item.quantity()) {
                System.out.println("""
                        
                        ============================================================
                        INVENTORY SERVICE - STOCK RESERVE FAILED
                        ============================================================
                        SKU           : %s
                        Available     : %s
                        Requested Qty : %s
                        Reason        : Insufficient stock
                        ============================================================
                        """.formatted(item.sku(), variant.getStock(), item.quantity()));
                throw new IllegalArgumentException("Insufficient stock for SKU " + item.sku());
            }
            variant.setStock(variant.getStock() - item.quantity());
            repository.save(variant);
            BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(item.quantity()));
            total = total.add(subtotal);
            response.getItems().add(new ReserveResponse.ReservedItemResponse(
                    variant.getSku(), variant.getProductName(), item.quantity(), variant.getPrice(), subtotal
            ));

            System.out.println("""
                    ------------------------------------------------------------
                    INVENTORY SERVICE - STOCK RESERVED
                    ------------------------------------------------------------
                    SKU           : %s
                    New Available : %s
                    Subtotal      : %s
                    ------------------------------------------------------------
                    """.formatted(variant.getSku(), variant.getStock(), subtotal));
        }
        response.setTotalAmount(total);
        return response;
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
        return items.stream().map(item -> item.getSku() + " x" + item.getQuantity()).collect(Collectors.joining(", ", "[", "]"));
    }

    private record ReserveRequestItem(String sku, Integer quantity) {
        ReserveRequestItem(com.velora.inventory.dto.ReserveItemRequest item) { this(item.getSku(), item.getQuantity()); }
    }
}
