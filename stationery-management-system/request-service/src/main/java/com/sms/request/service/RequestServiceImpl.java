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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);

    private final RequestRepository requestRepository;
    private final InventoryFeignClient inventoryClient;

    public RequestServiceImpl(RequestRepository requestRepository, InventoryFeignClient inventoryClient) {
        this.requestRepository = requestRepository;
        this.inventoryClient = inventoryClient;
    }

    @Override
    @Transactional
    public RequestResponseDTO submitRequest(SubmitRequestDTO dto, Long studentId, String studentEmail) {
        StationeryRequest request = new StationeryRequest();
        request.setStudentId(studentId);
        request.setStudentEmail(studentEmail);
        request.setStatus(StationeryRequest.Status.PENDING);

        List<RequestItem> items = new ArrayList<>();
        for (SubmitRequestDTO.ItemDTO itemDto : dto.getItems()) {
            // Validate item exists in Inventory Service via Feign
            ItemResponse invItem;
            try {
                invItem = inventoryClient.getItemById(itemDto.getItemId());
            } catch (FeignException.NotFound e) {
                throw new EntityNotFoundException("Inventory item not found with id: " + itemDto.getItemId());
            }

            // Validate enough stock
            if (invItem.getAvailableQuantity() < itemDto.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for item '" + invItem.getName() +
                        "'. Available: " + invItem.getAvailableQuantity() +
                        ", Requested: " + itemDto.getQuantity());
            }

            RequestItem requestItem = new RequestItem();
            requestItem.setRequest(request);
            requestItem.setItemId(invItem.getId());
            requestItem.setItemName(invItem.getName()); // denormalized snapshot
            requestItem.setQuantity(itemDto.getQuantity());
            items.add(requestItem);
        }

        request.setItems(items);
        StationeryRequest saved = requestRepository.save(request);
        log.info("Submitted request id={} by student={}", saved.getId(), studentEmail);
        return RequestResponseDTO.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponseDTO> getMyRequests(Long studentId) {
        return requestRepository.findByStudentIdOrderByRequestDateDesc(studentId)
                .stream().map(RequestResponseDTO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponseDTO> getAllRequests() {
        return requestRepository.findAllByOrderByRequestDateDesc()
                .stream().map(RequestResponseDTO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RequestResponseDTO getById(Long id) {
        StationeryRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found with id: " + id));
        return RequestResponseDTO.from(request);
    }

    @Override
    @Transactional
    public RequestResponseDTO approveRequest(Long requestId) {
        StationeryRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found with id: " + requestId));

        if (request.getStatus() != StationeryRequest.Status.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved. Current status: " + request.getStatus());
        }

        // Deduct stock for each item via Feign call
        for (RequestItem item : request.getItems()) {
            try {
                inventoryClient.deductQuantity(item.getItemId(), item.getQuantity());
                log.info("Deducted {} units of item id={} for request id={}", item.getQuantity(), item.getItemId(), requestId);
            } catch (FeignException e) {
                throw new IllegalStateException("Failed to deduct stock for item '" + item.getItemName() +
                        "': " + e.getMessage());
            }
        }

        request.setStatus(StationeryRequest.Status.APPROVED);
        StationeryRequest saved = requestRepository.save(request);
        log.info("Approved request id={}", requestId);
        return RequestResponseDTO.from(saved);
    }

    @Override
    @Transactional
    public RequestResponseDTO rejectRequest(Long requestId, String adminComment) {
        StationeryRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found with id: " + requestId));

        if (request.getStatus() != StationeryRequest.Status.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected. Current status: " + request.getStatus());
        }

        request.setStatus(StationeryRequest.Status.REJECTED);
        request.setAdminComment(adminComment);
        StationeryRequest saved = requestRepository.save(request);
        log.info("Rejected request id={} with comment: {}", requestId, adminComment);
        return RequestResponseDTO.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponseDTO> getByStatus(StationeryRequest.Status status) {
        return requestRepository.findByStatusOrderByRequestDateDesc(status)
                .stream().map(RequestResponseDTO::from).collect(Collectors.toList());
    }
}
