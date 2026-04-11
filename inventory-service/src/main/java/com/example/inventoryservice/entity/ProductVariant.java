package com.example.inventoryservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    private String sku;
    private String productId;
    private String colorName;
    private String sizeName;
    private BigDecimal price;
    private Integer availableStock;
    private Integer reservedStock;

    public ProductVariant() {
    }

    public ProductVariant(String sku, String productId, String colorName, String sizeName,
                          BigDecimal price, Integer availableStock, Integer reservedStock) {
        this.sku = sku;
        this.productId = productId;
        this.colorName = colorName;
        this.sizeName = sizeName;
        this.price = price;
        this.availableStock = availableStock;
        this.reservedStock = reservedStock;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getColorName() { return colorName; }
    public void setColorName(String colorName) { this.colorName = colorName; }
    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public Integer getReservedStock() { return reservedStock; }
    public void setReservedStock(Integer reservedStock) { this.reservedStock = reservedStock; }
}
