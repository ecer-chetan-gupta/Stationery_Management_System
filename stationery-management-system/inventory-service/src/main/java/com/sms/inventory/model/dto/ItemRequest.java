package com.sms.inventory.model.dto;

import com.sms.inventory.model.StationeryItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating a stationery item (ADMIN only).
 */
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Category is required (PAPER, PEN, PENCIL, NOTEBOOK, ERASER, OTHER)")
    private StationeryItem.Category category;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Available quantity cannot be negative")
    private Integer availableQuantity;

    @Min(value = 0, message = "Minimum quantity cannot be negative")
    private Integer minimumQuantity;

    public ItemRequest() {}

    public String getName()                              { return name; }
    public void setName(String name)                     { this.name = name; }
    public StationeryItem.Category getCategory()         { return category; }
    public void setCategory(StationeryItem.Category c)   { this.category = c; }
    public String getUnit()                              { return unit; }
    public void setUnit(String unit)                     { this.unit = unit; }
    public Integer getAvailableQuantity()                { return availableQuantity; }
    public void setAvailableQuantity(Integer qty)        { this.availableQuantity = qty; }
    public Integer getMinimumQuantity()                  { return minimumQuantity; }
    public void setMinimumQuantity(Integer minQty)       { this.minimumQuantity = minQty; }
}
