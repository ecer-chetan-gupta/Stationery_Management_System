package com.sms.request.service;

import com.sms.request.model.StationeryRequest;
import com.sms.request.model.dto.RequestResponseDTO;
import com.sms.request.model.dto.SubmitRequestDTO;

import java.util.List;

public interface RequestService {

    RequestResponseDTO submitRequest(SubmitRequestDTO dto, Long studentId, String studentEmail);

    List<RequestResponseDTO> getMyRequests(Long studentId);

    List<RequestResponseDTO> getAllRequests();

    RequestResponseDTO getById(Long id);

    RequestResponseDTO approveRequest(Long requestId);

    RequestResponseDTO rejectRequest(Long requestId, String adminComment);

    List<RequestResponseDTO> getByStatus(StationeryRequest.Status status);
}
