package com.sms.request.model;

import jakarta.persistence.*;

/**
 * RequestItem — line items within a StationeryRequest.
 * Stores a denormalized snapshot of item name to avoid cross-service joins.
 */
@Entity
@Table(name = "request_items")
public class RequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private StationeryRequest request;

    @Column(nullable = false)
    private Long itemId;          // cross-service reference (no FK constraint)

    @Column(nullable = false, length = 200)
    private String itemName;      // denormalized snapshot from Inventory Service

    @Column(nullable = false)
    private Integer quantity;

    public RequestItem() {}

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public StationeryRequest getRequest()        { return request; }
    public void setRequest(StationeryRequest r)  { this.request = r; }
    public Long getItemId()                      { return itemId; }
    public void setItemId(Long itemId)           { this.itemId = itemId; }
    public String getItemName()                  { return itemName; }
    public void setItemName(String itemName)     { this.itemName = itemName; }
    public Integer getQuantity()                 { return quantity; }
    public void setQuantity(Integer qty)         { this.quantity = qty; }
}
