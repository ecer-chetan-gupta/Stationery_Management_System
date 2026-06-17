package com.sms.inventory.repository;

import com.sms.inventory.model.StationeryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationeryRepository extends JpaRepository<StationeryItem, Long> {

    List<StationeryItem> findByCategory(StationeryItem.Category category);

    List<StationeryItem> findByNameContainingIgnoreCase(String name);

    /** Items where availableQuantity <= minimumQuantity (low-stock alert) */
    @Query("SELECT s FROM StationeryItem s WHERE s.minimumQuantity IS NOT NULL AND s.availableQuantity <= s.minimumQuantity")
    List<StationeryItem> findLowStockItems();

    Page<StationeryItem> findAll(Pageable pageable);

    Page<StationeryItem> findByCategory(StationeryItem.Category category, Pageable pageable);
}
