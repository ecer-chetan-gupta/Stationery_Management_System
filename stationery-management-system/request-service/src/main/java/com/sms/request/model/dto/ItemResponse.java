package com.sms.request.model.dto;

/**
 * Mirror of inventory-service's ItemResponse.
 * Used as the return type for InventoryFeignClient.getItemById().
 */
public class ItemResponse {
    private Long id;
    private String name;
    private String category;
    private String unit;
    private Integer availableQuantity;
    private Integer minimumQuantity;
    private boolean lowStock;

    public ItemResponse() {}

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }
    public String getCategory()                      { return category; }
    public void setCategory(String category)         { this.category = category; }
    public String getUnit()                          { return unit; }
    public void setUnit(String unit)                 { this.unit = unit; }
    public Integer getAvailableQuantity()            { return availableQuantity; }
    public void setAvailableQuantity(Integer qty)    { this.availableQuantity = qty; }
    public Integer getMinimumQuantity()              { return minimumQuantity; }
    public void setMinimumQuantity(Integer minQty)   { this.minimumQuantity = minQty; }
    public boolean isLowStock()                      { return lowStock; }
    public void setLowStock(boolean lowStock)        { this.lowStock = lowStock; }
}
