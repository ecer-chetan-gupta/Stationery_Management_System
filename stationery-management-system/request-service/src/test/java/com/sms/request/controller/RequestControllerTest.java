package com.sms.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.request.model.StationeryRequest;
import com.sms.request.model.dto.RequestResponseDTO;
import com.sms.request.model.dto.SubmitRequestDTO;
import com.sms.request.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RequestController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;

    private SubmitRequestDTO submitDTO;
    private RequestResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        submitDTO = new SubmitRequestDTO();
        SubmitRequestDTO.ItemDTO itemDTO = new SubmitRequestDTO.ItemDTO();
        itemDTO.setItemId(1L);
        itemDTO.setQuantity(5);
        submitDTO.setItems(List.of(itemDTO));

        responseDTO = new RequestResponseDTO();
        responseDTO.setId(10L);
        responseDTO.setStudentId(100L);
        responseDTO.setStudentEmail("student@sms.com");
        responseDTO.setStatus("PENDING");

        RequestResponseDTO.ItemDTO itemResponse = new RequestResponseDTO.ItemDTO();
        itemResponse.itemId = 1L;
        itemResponse.itemName = "Notebook";
        itemResponse.quantity = 5;
        responseDTO.setItems(List.of(itemResponse));
    }

    @Test
    void submitRequest_Success() throws Exception {
        when(requestService.submitRequest(any(SubmitRequestDTO.class), eq(100L), eq("student@sms.com")))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitDTO))
                        .header("X-Auth-User-Id", "100")
                        .header("X-Auth-User-Email", "student@sms.com")
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void submitRequest_BadRequest_WhenEmptyItems() throws Exception {
        submitDTO.setItems(Collections.emptyList());

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitDTO))
                        .header("X-Auth-User-Id", "100")
                        .header("X-Auth-User-Email", "student@sms.com")
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyRequests_Success() throws Exception {
        when(requestService.getMyRequests(100L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/requests/my")
                        .header("X-Auth-User-Id", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].studentId").value(100));
    }

    @Test
    void getAllRequests_Forbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/requests")
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllRequests_Success_WhenAdmin() throws Exception {
        when(requestService.getAllRequests()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/requests")
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void getById_Success() throws Exception {
        when(requestService.getById(10L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/requests/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void approve_Forbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(put("/api/requests/{id}/approve", 10L)
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approve_Success_WhenAdmin() throws Exception {
        responseDTO.setStatus("APPROVED");
        when(requestService.approveRequest(10L)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/requests/{id}/approve", 10L)
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void reject_Forbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(put("/api/requests/{id}/reject", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("adminComment", "No budget")))
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reject_Success_WhenAdmin() throws Exception {
        responseDTO.setStatus("REJECTED");
        responseDTO.setAdminComment("No budget");
        when(requestService.rejectRequest(10L, "No budget")).thenReturn(responseDTO);

        mockMvc.perform(put("/api/requests/{id}/reject", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("adminComment", "No budget")))
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.adminComment").value("No budget"));
    }

    @Test
    void getByStatus_Forbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/requests/status/pending")
                        .header("X-Auth-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getByStatus_Success_WhenAdmin() throws Exception {
        when(requestService.getByStatus(StationeryRequest.Status.PENDING)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/requests/status/pending")
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getByStatus_BadRequest_WhenInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/requests/status/invalid_status")
                        .header("X-Auth-User-Role", "ADMIN"))
                .andExpect(status().isBadRequest());
    }
}
