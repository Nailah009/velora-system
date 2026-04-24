package com.velora.inventory.config;

import com.velora.inventory.model.ProductVariant;
import com.velora.inventory.repository.ProductVariantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedProducts(ProductVariantRepository repository) {
        return args -> {
            seed(repository, "VLR-TSH-BLK-M", "Velora Oversized T-Shirt", "T-Shirt", "Black", "M", new BigDecimal("129000"), 50);
            seed(repository, "VLR-TSH-WHT-L", "Velora Basic White T-Shirt", "T-Shirt", "White", "L", new BigDecimal("99000"), 60);
            seed(repository, "VLR-HOODIE-BLK-L", "Velora Hoodie Black", "Hoodie", "Black", "L", new BigDecimal("250000"), 30);
            seed(repository, "VLR-HOODIE-GRY-M", "Velora Hoodie Grey", "Hoodie", "Grey", "M", new BigDecimal("245000"), 25);
            seed(repository, "VLR-JKT-DNM-L", "Velora Denim Jacket", "Jacket", "Blue", "L", new BigDecimal("350000"), 20);
            seed(repository, "VLR-JKT-BMB-M", "Velora Bomber Jacket", "Jacket", "Olive", "M", new BigDecimal("320000"), 15);
            seed(repository, "VLR-PNT-KHK-32", "Velora Chino Pants", "Pants", "Khaki", "32", new BigDecimal("220000"), 35);
            seed(repository, "VLR-PNT-BLK-30", "Velora Slim Fit Pants", "Pants", "Black", "30", new BigDecimal("210000"), 28);
            seed(repository, "VLR-SHT-BLU-M", "Velora Casual Shirt", "Shirt", "Blue", "M", new BigDecimal("175000"), 40);
            seed(repository, "VLR-SHT-WHT-L", "Velora Formal Shirt", "Shirt", "White", "L", new BigDecimal("190000"), 32);
            seed(repository, "VLR-DRS-BRN-S", "Velora Midi Dress", "Dress", "Brown", "S", new BigDecimal("275000"), 18);
            seed(repository, "VLR-SKR-BLK-M", "Velora Pleated Skirt", "Skirt", "Black", "M", new BigDecimal("165000"), 22);
        };
    }

    private void seed(ProductVariantRepository repository, String sku, String name, String category, String color, String size, BigDecimal price, int stock) {
        if (!repository.existsBySku(sku)) {
            ProductVariant v = new ProductVariant();
            v.setSku(sku);
            v.setProductName(name);
            v.setCategory(category);
            v.setColor(color);
            v.setSize(size);
            v.setPrice(price);
            v.setStock(stock);
            repository.save(v);
        }
    }
}
