package com.sms.inventory.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * StationeryItem entity — maps to `stationery_items` in inventory_db.
 */
@Entity
@Table(name = "stationery_items")
@EntityListeners(AuditingEntityListener.class)
public class StationeryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(length = 50)
    private String unit; // e.g. "pieces", "packs"

    @Column(nullable = false)
    private Integer availableQuantity = 0;

    private Integer minimumQuantity; // low-stock threshold

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Category { PAPER, PEN, PENCIL, NOTEBOOK, ERASER, OTHER }

    // ─── No-arg constructor ────────────────────────────────────────────────────
    public StationeryItem() {}

    public StationeryItem(Long id, String name, Category category, String unit,
                          Integer availableQuantity, Integer minimumQuantity,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.availableQuantity = availableQuantity;
        this.minimumQuantity = minimumQuantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─── Builder ──────────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private Category category;
        private String unit;
        private Integer availableQuantity = 0;
        private Integer minimumQuantity;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id)                              { this.id = id; return this; }
        public Builder name(String name)                        { this.name = name; return this; }
        public Builder category(Category category)              { this.category = category; return this; }
        public Builder unit(String unit)                        { this.unit = unit; return this; }
        public Builder availableQuantity(Integer qty)           { this.availableQuantity = qty; return this; }
        public Builder minimumQuantity(Integer minQty)          { this.minimumQuantity = minQty; return this; }
        public Builder createdAt(LocalDateTime createdAt)       { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt)       { this.updatedAt = updatedAt; return this; }

        public StationeryItem build() {
            return new StationeryItem(id, name, category, unit, availableQuantity,
                    minimumQuantity, createdAt, updatedAt);
        }
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }
    public Category getCategory()                    { return category; }
    public void setCategory(Category category)       { this.category = category; }
    public String getUnit()                          { return unit; }
    public void setUnit(String unit)                 { this.unit = unit; }
    public Integer getAvailableQuantity()            { return availableQuantity; }
    public void setAvailableQuantity(Integer qty)    { this.availableQuantity = qty; }
    public Integer getMinimumQuantity()              { return minimumQuantity; }
    public void setMinimumQuantity(Integer minQty)   { this.minimumQuantity = minQty; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime v)        { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)        { this.updatedAt = v; }

    /** True if stock is at or below the minimum threshold */
    public boolean isLowStock() {
        return minimumQuantity != null && availableQuantity <= minimumQuantity;
    }
}
