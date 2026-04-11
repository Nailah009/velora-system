package com.example.inventoryservice.repository;

import com.example.inventoryservice.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProductIdOrderBySkuAsc(String productId);
}
