package com.velora.inventory.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReserveResponse {
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private List<ReservedItemResponse> items = new ArrayList<>();

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<ReservedItemResponse> getItems() { return items; }
    public void setItems(List<ReservedItemResponse> items) { this.items = items; }

    public static class ReservedItemResponse {
        private String sku;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;

        public ReservedItemResponse() {}
        public ReservedItemResponse(String sku, String productName, Integer quantity, BigDecimal price, BigDecimal subtotal) {
            this.sku = sku;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.subtotal = subtotal;
        }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}
