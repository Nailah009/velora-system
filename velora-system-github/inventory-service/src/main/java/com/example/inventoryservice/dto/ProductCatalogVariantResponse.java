package com.example.inventoryservice.dto;

import java.math.BigDecimal;

public class ProductCatalogVariantResponse {

    private String sku;
    private String colorName;
    private String sizeName;
    private BigDecimal price;
    private Integer availableStock;
    private Integer reservedStock;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public Integer getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(Integer reservedStock) {
        this.reservedStock = reservedStock;
    }
}