package com.sms.inventory.service;

import com.sms.inventory.exception.InsufficientStockException;
import com.sms.inventory.model.StationeryItem;
import com.sms.inventory.model.dto.ItemRequest;
import com.sms.inventory.model.dto.ItemResponse;
import com.sms.inventory.repository.StationeryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StationeryServiceImpl implements StationeryService {

    private static final Logger log = LoggerFactory.getLogger(StationeryServiceImpl.class);

    private final StationeryRepository repository;

    public StationeryServiceImpl(StationeryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ItemResponse addItem(ItemRequest request) {
        StationeryItem item = StationeryItem.builder()
                .name(request.getName().trim())
                .category(request.getCategory())
                .unit(request.getUnit())
                .availableQuantity(request.getAvailableQuantity())
                .minimumQuantity(request.getMinimumQuantity())
                .build();
        StationeryItem saved = repository.save(item);
        log.info("Created stationery item: {} (id={})", saved.getName(), saved.getId());
        return ItemResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return repository.findAll(pageable).map(ItemResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponse> getByCategory(StationeryItem.Category category, Pageable pageable) {
        return repository.findByCategory(category, pageable).map(ItemResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        StationeryItem item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        return ItemResponse.from(item);
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        StationeryItem item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        item.setName(request.getName().trim());
        item.setCategory(request.getCategory());
        item.setUnit(request.getUnit());
        item.setAvailableQuantity(request.getAvailableQuantity());
        item.setMinimumQuantity(request.getMinimumQuantity());
        StationeryItem updated = repository.save(item);
        log.info("Updated stationery item: {} (id={})", updated.getName(), updated.getId());
        return ItemResponse.from(updated);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Item not found with id: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted stationery item id={}", id);
    }

    @Override
    @Transactional
    public void deductQuantity(Long id, int qty) {
        StationeryItem item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        if (item.getAvailableQuantity() < qty) {
            throw new InsufficientStockException(
                    "Insufficient stock for item '" + item.getName() +
                    "'. Requested: " + qty + ", Available: " + item.getAvailableQuantity()
            );
        }
        item.setAvailableQuantity(item.getAvailableQuantity() - qty);
        repository.save(item);
        log.info("Deducted {} units from item '{}' (id={}). Remaining: {}",
                qty, item.getName(), id, item.getAvailableQuantity());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getLowStockItems() {
        return repository.findLowStockItems().stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> searchItems(String query) {
        return repository.findByNameContainingIgnoreCase(query).stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }
}
