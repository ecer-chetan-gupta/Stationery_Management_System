package com.sms.inventory.model.dto;

import com.sms.inventory.model.StationeryItem;
import java.time.LocalDateTime;

/**
 * DTO returned in API responses for stationery items.
 * Also used by InventoryFeignClient in request-service.
 */
public class ItemResponse {

    private Long id;
    private String name;
    private StationeryItem.Category category;
    private String unit;
    private Integer availableQuantity;
    private Integer minimumQuantity;
    private boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItemResponse() {}

    // ─── Factory method from entity ───────────────────────────────────────────
    public static ItemResponse from(StationeryItem item) {
        ItemResponse dto = new ItemResponse();
        dto.id = item.getId();
        dto.name = item.getName();
        dto.category = item.getCategory();
        dto.unit = item.getUnit();
        dto.availableQuantity = item.getAvailableQuantity();
        dto.minimumQuantity = item.getMinimumQuantity();
        dto.lowStock = item.isLowStock();
        dto.createdAt = item.getCreatedAt();
        dto.updatedAt = item.getUpdatedAt();
        return dto;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }
    public StationeryItem.Category getCategory()     { return category; }
    public void setCategory(StationeryItem.Category c) { this.category = c; }
    public String getUnit()                          { return unit; }
    public void setUnit(String unit)                 { this.unit = unit; }
    public Integer getAvailableQuantity()            { return availableQuantity; }
    public void setAvailableQuantity(Integer qty)    { this.availableQuantity = qty; }
    public Integer getMinimumQuantity()              { return minimumQuantity; }
    public void setMinimumQuantity(Integer minQty)   { this.minimumQuantity = minQty; }
    public boolean isLowStock()                      { return lowStock; }
    public void setLowStock(boolean lowStock)        { this.lowStock = lowStock; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime v)        { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)        { this.updatedAt = v; }
}
