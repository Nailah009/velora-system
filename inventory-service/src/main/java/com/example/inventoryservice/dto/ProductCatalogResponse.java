package com.example.inventoryservice.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductCatalogResponse {

    private String id;
    private String name;
    private String brand;
    private String category;
    private BigDecimal startingPrice;
    private BigDecimal highestPrice;
    private Integer totalAvailableStock;
    private List<String> availableColors;
    private List<String> availableSizes;
    private List<ProductCatalogVariantResponse> variants;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(BigDecimal highestPrice) {
        this.highestPrice = highestPrice;
    }

    public Integer getTotalAvailableStock() {
        return totalAvailableStock;
    }

    public void setTotalAvailableStock(Integer totalAvailableStock) {
        this.totalAvailableStock = totalAvailableStock;
    }

    public List<String> getAvailableColors() {
        return availableColors;
    }

    public void setAvailableColors(List<String> availableColors) {
        this.availableColors = availableColors;
    }

    public List<String> getAvailableSizes() {
        return availableSizes;
    }

    public void setAvailableSizes(List<String> availableSizes) {
        this.availableSizes = availableSizes;
    }

    public List<ProductCatalogVariantResponse> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductCatalogVariantResponse> variants) {
        this.variants = variants;
    }
}