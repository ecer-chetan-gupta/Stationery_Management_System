package com.sms.request.service;

import com.sms.request.feign.InventoryFeignClient;
import com.sms.request.model.RequestItem;
import com.sms.request.model.StationeryRequest;
import com.sms.request.model.dto.ItemResponse;
import com.sms.request.model.dto.RequestResponseDTO;
import com.sms.request.model.dto.SubmitRequestDTO;
import com.sms.request.repository.RequestRepository;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private InventoryFeignClient inventoryClient;

    @InjectMocks
    private RequestServiceImpl requestService;

    private SubmitRequestDTO submitDTO;
    private StationeryRequest pendingRequest;
    private ItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        submitDTO = new SubmitRequestDTO();
        SubmitRequestDTO.ItemDTO itemDTO = new SubmitRequestDTO.ItemDTO();
        itemDTO.setItemId(1L);
        itemDTO.setQuantity(5);
        submitDTO.setItems(List.of(itemDTO));

        itemResponse = new ItemResponse();
        itemResponse.setId(1L);
        itemResponse.setName("Notebook");
        itemResponse.setAvailableQuantity(10);

        pendingRequest = new StationeryRequest();
        pendingRequest.setId(10L);
        pendingRequest.setStudentId(100L);
        pendingRequest.setStudentEmail("student@sms.com");
        pendingRequest.setStatus(StationeryRequest.Status.PENDING);

        RequestItem requestItem = new RequestItem();
        requestItem.setId(1L);
        requestItem.setItemId(1L);
        requestItem.setItemName("Notebook");
        requestItem.setQuantity(5);
        requestItem.setRequest(pendingRequest);

        pendingRequest.setItems(List.of(requestItem));
    }

    @Test
    void submitRequest_Success() {
        when(inventoryClient.getItemById(1L)).thenReturn(itemResponse);
        when(requestRepository.save(any(StationeryRequest.class))).thenAnswer(invocation -> {
            StationeryRequest saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        RequestResponseDTO response = requestService.submitRequest(submitDTO, 100L, "student@sms.com");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals("Notebook", response.getItems().get(0).itemName);
        verify(inventoryClient, times(1)).getItemById(1L);
        verify(requestRepository, times(1)).save(any(StationeryRequest.class));
    }

    @Test
    void submitRequest_ThrowsEntityNotFoundException_WhenItemNotFound() {
        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.GET,
                "url",
                java.util.Collections.emptyMap(),
                null,
                new feign.RequestTemplate()
        );
        feign.FeignException.NotFound notFoundEx = new feign.FeignException.NotFound(
                "Not Found",
                request,
                new byte[0],
                java.util.Collections.emptyMap()
        );
        when(inventoryClient.getItemById(1L)).thenThrow(notFoundEx);

        assertThrows(EntityNotFoundException.class, () -> 
                requestService.submitRequest(submitDTO, 100L, "student@sms.com"));

        verify(inventoryClient, times(1)).getItemById(1L);
        verify(requestRepository, never()).save(any(StationeryRequest.class));
    }

    @Test
    void submitRequest_ThrowsIllegalStateException_WhenInsufficientStock() {
        itemResponse.setAvailableQuantity(2); // Only 2 available, but requested 5
        when(inventoryClient.getItemById(1L)).thenReturn(itemResponse);

        assertThrows(IllegalStateException.class, () -> 
                requestService.submitRequest(submitDTO, 100L, "student@sms.com"));

        verify(inventoryClient, times(1)).getItemById(1L);
        verify(requestRepository, never()).save(any(StationeryRequest.class));
    }

    @Test
    void getMyRequests_Success() {
        when(requestRepository.findByStudentIdOrderByRequestDateDesc(100L)).thenReturn(List.of(pendingRequest));

        List<RequestResponseDTO> response = requestService.getMyRequests(100L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).getId());
        verify(requestRepository, times(1)).findByStudentIdOrderByRequestDateDesc(100L);
    }

    @Test
    void getAllRequests_Success() {
        when(requestRepository.findAllByOrderByRequestDateDesc()).thenReturn(List.of(pendingRequest));

        List<RequestResponseDTO> response = requestService.getAllRequests();

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(requestRepository, times(1)).findAllByOrderByRequestDateDesc();
    }

    @Test
    void getById_Success() {
        when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));

        RequestResponseDTO response = requestService.getById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        verify(requestRepository, times(1)).findById(10L);
    }

    @Test
    void getById_ThrowsEntityNotFoundException() {
        when(requestRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> requestService.getById(10L));
        verify(requestRepository, times(1)).findById(10L);
    }

    @Test
    void approveRequest_Success() {
        when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        doNothing().when(inventoryClient).deductQuantity(1L, 5);
        when(requestRepository.save(any(StationeryRequest.class))).thenReturn(pendingRequest);

        RequestResponseDTO response = requestService.approveRequest(10L);

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        verify(inventoryClient, times(1)).deductQuantity(1L, 5);
        verify(requestRepository, times(1)).save(pendingRequest);
    }

    @Test
    void approveRequest_ThrowsIllegalStateException_WhenNotPending() {
        pendingRequest.setStatus(StationeryRequest.Status.REJECTED);
        when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));

        assertThrows(IllegalStateException.class, () -> requestService.approveRequest(10L));
        verify(inventoryClient, never()).deductQuantity(anyLong(), anyInt());
        verify(requestRepository, never()).save(any(StationeryRequest.class));
    }

    @Test
    void approveRequest_ThrowsIllegalStateException_WhenFeignCallFails() {
        when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.PUT,
                "url",
                java.util.Collections.emptyMap(),
                null,
                new feign.RequestTemplate()
        );
        feign.FeignException feignEx = new feign.FeignException.InternalServerError(
                "Internal Server Error",
                request,
                new byte[0],
                java.util.Collections.emptyMap()
        );
        doThrow(feignEx).when(inventoryClient).deductQuantity(1L, 5);

        assertThrows(IllegalStateException.class, () -> requestService.approveRequest(10L));
        verify(requestRepository, never()).save(any(StationeryRequest.class));
    }

    @Test
    void rejectRequest_Success() {
        when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepository.save(any(StationeryRequest.class))).thenReturn(pendingRequest);

        RequestResponseDTO response = requestService.rejectRequest(10L, "Not needed");

        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertEquals("Not needed", pendingRequest.getAdminComment());
        verify(requestRepository, times(1)).save(pendingRequest);
    }

    @Test
    void rejectRequest_ThrowsIllegalStateException_WhenNotPending() {
        pendingRequest.setStatus(StationeryRequest.Status.APPROVED);
        when(requestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));

        assertThrows(IllegalStateException.class, () -> requestService.rejectRequest(10L, "Reason"));
        verify(requestRepository, never()).save(any(StationeryRequest.class));
    }

    @Test
    void getByStatus_Success() {
        when(requestRepository.findByStatusOrderByRequestDateDesc(StationeryRequest.Status.PENDING))
                .thenReturn(List.of(pendingRequest));

        List<RequestResponseDTO> response = requestService.getByStatus(StationeryRequest.Status.PENDING);

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(requestRepository, times(1)).findByStatusOrderByRequestDateDesc(StationeryRequest.Status.PENDING);
    }
}
