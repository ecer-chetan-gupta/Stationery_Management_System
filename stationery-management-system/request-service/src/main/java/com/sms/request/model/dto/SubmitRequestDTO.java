package com.sms.request.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * DTO for submitting a new stationery request (STUDENT).
 */
public class SubmitRequestDTO {

    @NotEmpty(message = "Request must have at least one item")
    @Valid
    private List<ItemDTO> items;

    public List<ItemDTO> getItems() { return items; }
    public void setItems(List<ItemDTO> items) { this.items = items; }

    public static class ItemDTO {
        @NotNull(message = "Item ID is required")
        private Long itemId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        public Long getItemId()              { return itemId; }
        public void setItemId(Long itemId)   { this.itemId = itemId; }
        public Integer getQuantity()         { return quantity; }
        public void setQuantity(Integer qty) { this.quantity = qty; }
    }
}
