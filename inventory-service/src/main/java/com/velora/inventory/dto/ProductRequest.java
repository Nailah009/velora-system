package com.velora.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductRequest {
    @NotBlank(message = "SKU is required")
    private String sku;
    @NotBlank(message = "Product name is required")
    private String productName;
    @NotBlank(message = "Category is required")
    private String category;
    @NotBlank(message = "Color is required")
    private String color;
    @NotBlank(message = "Size is required")
    private String size;
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
