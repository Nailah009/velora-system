package com.example.inventoryservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    private String sku;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String colorName;
    private String sizeName;
    private BigDecimal price;
    private Integer availableStock;
    private Integer reservedStock;

    public ProductVariant() {
    }

    public ProductVariant(String sku, Product product, String colorName, String sizeName,
                          BigDecimal price, Integer availableStock, Integer reservedStock) {
        this.sku = sku;
        this.product = product;
        this.colorName = colorName;
        this.sizeName = sizeName;
        this.price = price;
        this.availableStock = availableStock;
        this.reservedStock = reservedStock;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    @JsonProperty("productId")
    public String getProductId() {
        return product != null ? product.getId() : null;
    }

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
