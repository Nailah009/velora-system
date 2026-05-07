package com.velora.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ReserveRequest {
    @Valid
    @NotEmpty(message = "Items cannot be empty")
    private List<ReserveItemRequest> items;

    public List<ReserveItemRequest> getItems() { return items; }
    public void setItems(List<ReserveItemRequest> items) { this.items = items; }
}
