package com.sms.inventory.service;

import com.sms.inventory.model.StationeryItem;
import com.sms.inventory.model.dto.ItemRequest;
import com.sms.inventory.model.dto.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StationeryService {

    ItemResponse addItem(ItemRequest request);

    Page<ItemResponse> getAllItems(Pageable pageable);

    Page<ItemResponse> getByCategory(StationeryItem.Category category, Pageable pageable);

    ItemResponse getById(Long id);

    ItemResponse updateItem(Long id, ItemRequest request);

    void deleteItem(Long id);

    /**
     * Deduct qty from availableQuantity.
     * Called internally by Request Service via Feign.
     * Throws InsufficientStockException if stock < qty.
     */
    void deductQuantity(Long id, int qty);

    List<ItemResponse> getLowStockItems();

    List<ItemResponse> searchItems(String query);
}
