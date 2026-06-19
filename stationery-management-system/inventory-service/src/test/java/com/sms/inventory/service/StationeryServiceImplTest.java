package com.sms.inventory.service;

import com.sms.inventory.exception.InsufficientStockException;
import com.sms.inventory.model.StationeryItem;
import com.sms.inventory.model.dto.ItemRequest;
import com.sms.inventory.model.dto.ItemResponse;
import com.sms.inventory.repository.StationeryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationeryServiceImplTest {

    @Mock
    private StationeryRepository repository;

    @InjectMocks
    private StationeryServiceImpl service;

    private ItemRequest itemRequest;
    private StationeryItem item;

    @BeforeEach
    void setUp() {
        itemRequest = new ItemRequest();
        itemRequest.setName("Gel Pen");
        itemRequest.setCategory(StationeryItem.Category.PEN);
        itemRequest.setUnit("pack");
        itemRequest.setAvailableQuantity(50);
        itemRequest.setMinimumQuantity(5);

        item = StationeryItem.builder()
                .id(1L)
                .name("Gel Pen")
                .category(StationeryItem.Category.PEN)
                .unit("pack")
                .availableQuantity(50)
                .minimumQuantity(5)
                .build();
    }

    @Test
    void addItem_Success() {
        when(repository.save(any(StationeryItem.class))).thenReturn(item);

        ItemResponse response = service.addItem(itemRequest);

        assertNotNull(response);
        assertEquals("Gel Pen", response.getName());
        verify(repository, times(1)).save(any(StationeryItem.class));
    }

    @Test
    void getAllItems_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<StationeryItem> page = new PageImpl<>(List.of(item));
        when(repository.findAll(pageable)).thenReturn(page);

        Page<ItemResponse> response = service.getAllItems(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Gel Pen", response.getContent().get(0).getName());
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void getById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        ItemResponse response = service.getById(1L);

        assertNotNull(response);
        assertEquals("Gel Pen", response.getName());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void getById_ThrowsEntityNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getById(1L));
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void updateItem_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(StationeryItem.class))).thenReturn(item);

        ItemResponse response = service.updateItem(1L, itemRequest);

        assertNotNull(response);
        assertEquals("Gel Pen", response.getName());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(StationeryItem.class));
    }

    @Test
    void deleteItem_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteItem(1L));

        verify(repository, times(1)).existsById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteItem_ThrowsEntityNotFoundException() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.deleteItem(1L));
        verify(repository, times(1)).existsById(1L);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void deductQuantity_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(StationeryItem.class))).thenReturn(item);

        assertDoesNotThrow(() -> service.deductQuantity(1L, 10));

        assertEquals(40, item.getAvailableQuantity());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(StationeryItem.class));
    }

    @Test
    void deductQuantity_ThrowsInsufficientStockException() {
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(InsufficientStockException.class, () -> service.deductQuantity(1L, 60));
        verify(repository, times(1)).findById(1L);
        verify(repository, never()).save(any(StationeryItem.class));
    }

    @Test
    void getLowStockItems_ReturnsCorrectList() {
        when(repository.findLowStockItems()).thenReturn(List.of(item));

        List<ItemResponse> response = service.getLowStockItems();

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(repository, times(1)).findLowStockItems();
    }

    @Test
    void searchItems_Success() {
        when(repository.findByNameContainingIgnoreCase("pen")).thenReturn(List.of(item));

        List<ItemResponse> response = service.searchItems("pen");

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(repository, times(1)).findByNameContainingIgnoreCase("pen");
    }
}
