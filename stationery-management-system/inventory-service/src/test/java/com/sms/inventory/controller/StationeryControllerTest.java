package com.sms.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.inventory.model.StationeryItem;
import com.sms.inventory.model.dto.ItemRequest;
import com.sms.inventory.model.dto.ItemResponse;
import com.sms.inventory.service.StationeryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StationeryController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class StationeryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StationeryService stationeryService;

    private ItemRequest validRequest;
    private ItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ItemRequest();
        validRequest.setName("Gel Pen");
        validRequest.setCategory(StationeryItem.Category.PEN);
        validRequest.setUnit("pack");
        validRequest.setAvailableQuantity(50);
        validRequest.setMinimumQuantity(5);

        itemResponse = new ItemResponse();
        itemResponse.setId(1L);
        itemResponse.setName("Gel Pen");
        itemResponse.setCategory(StationeryItem.Category.PEN);
        itemResponse.setUnit("pack");
        itemResponse.setAvailableQuantity(50);
        itemResponse.setMinimumQuantity(5);
        itemResponse.setLowStock(false);
        itemResponse.setCreatedAt(LocalDateTime.now());
        itemResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllItems_Success() throws Exception {
        Page<ItemResponse> page = new PageImpl<>(List.of(itemResponse));
        when(stationeryService.getAllItems(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/inventory")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Gel Pen"))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getAllItems_WithCategory_Success() throws Exception {
        Page<ItemResponse> page = new PageImpl<>(List.of(itemResponse));
        when(stationeryService.getByCategory(eq(StationeryItem.Category.PEN), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/inventory")
                        .param("category", "PEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("PEN"));
    }

    @Test
    void getById_Success() throws Exception {
        when(stationeryService.getById(1L)).thenReturn(itemResponse);

        mockMvc.perform(get("/api/inventory/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gel Pen"));
    }

    @Test
    void search_Success() throws Exception {
        when(stationeryService.searchItems("Gel")).thenReturn(List.of(itemResponse));

        mockMvc.perform(get("/api/inventory/search")
                        .param("q", "Gel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gel Pen"));
    }

    @Test
    void getLowStock_Forbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/inventory/low-stock")
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLowStock_Success_WhenAdmin() throws Exception {
        when(stationeryService.getLowStockItems()).thenReturn(List.of(itemResponse));

        mockMvc.perform(get("/api/inventory/low-stock")
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gel Pen"));
    }

    @Test
    void addItem_Forbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addItem_BadRequest_WhenInvalidPayload() throws Exception {
        validRequest.setName(""); // Blank name is invalid

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_Success_WhenAdmin() throws Exception {
        when(stationeryService.addItem(any(ItemRequest.class))).thenReturn(itemResponse);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Gel Pen"));
    }

    @Test
    void updateItem_Forbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(put("/api/inventory/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateItem_Success_WhenAdmin() throws Exception {
        when(stationeryService.updateItem(eq(1L), any(ItemRequest.class))).thenReturn(itemResponse);

        mockMvc.perform(put("/api/inventory/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gel Pen"));
    }

    @Test
    void deleteItem_Forbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/inventory/{id}", 1L)
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteItem_Success_WhenAdmin() throws Exception {
        doNothing().when(stationeryService).deleteItem(1L);

        mockMvc.perform(delete("/api/inventory/{id}", 1L)
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());

        verify(stationeryService, times(1)).deleteItem(1L);
    }

    @Test
    void deductQuantity_Success() throws Exception {
        doNothing().when(stationeryService).deductQuantity(1L, 5);

        mockMvc.perform(put("/api/inventory/{id}/deduct", 1L)
                        .param("qty", "5"))
                .andExpect(status().isOk());

        verify(stationeryService, times(1)).deductQuantity(1L, 5);
    }
}
