package com.example.inventoryservice.repository;

import com.example.inventoryservice.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProduct_IdOrderBySkuAsc(String productId);

    @Query("select pv from ProductVariant pv join fetch pv.product p order by p.id asc, pv.sku asc")
    List<ProductVariant> findAllWithProduct();
}
