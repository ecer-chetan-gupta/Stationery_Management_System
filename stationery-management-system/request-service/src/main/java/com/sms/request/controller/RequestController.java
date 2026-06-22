package com.sms.request.controller;

import com.sms.request.model.StationeryRequest;
import com.sms.request.model.dto.RequestResponseDTO;
import com.sms.request.model.dto.SubmitRequestDTO;
import com.sms.request.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Request REST Controller
 * Base path: /api/requests
 *
 * Headers from Gateway (set by JwtAuthFilter after validation):
 *   X-Auth-User-Id    -> Long studentId
 *   X-Auth-User-Email -> String studentEmail
 *   X-Auth-User-Role  -> "ADMIN" or "STUDENT"
 */
@RestController
@RequestMapping("/api/requests")
@Tag(name = "Requests", description = "Stationery request lifecycle management")
public class RequestController {

    private static final Logger log = LoggerFactory.getLogger(RequestController.class);

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    // --- POST: Submit new request (STUDENT) -----------------------------------

    @PostMapping
    @Operation(summary = "Submit a new stationery request (STUDENT)")
    public ResponseEntity<?> submit(
            @Valid @RequestBody SubmitRequestDTO dto,
            @RequestHeader(value = "X-Auth-User-Id", defaultValue = "0") Long studentId,
            @RequestHeader(value = "X-Auth-User-Email", defaultValue = "") String studentEmail,
            @RequestHeader(value = "X-Auth-User-Role", defaultValue = "") String role) {

        RequestResponseDTO response = requestService.submitRequest(dto, studentId, studentEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- GET: My requests (STUDENT) -------------------------------------------

    @GetMapping("/my")
    @Operation(summary = "Get my stationery requests (STUDENT)")
    public ResponseEntity<List<RequestResponseDTO>> getMyRequests(
            @RequestHeader(value = "X-Auth-User-Id", defaultValue = "0") Long studentId) {
        return ResponseEntity.ok(requestService.getMyRequests(studentId));
    }

    // --- GET: All requests (ADMIN) --------------------------------------------

    @GetMapping
    @Operation(summary = "Get all stationery requests (ADMIN only)")
    public ResponseEntity<?> getAllRequests(
            @RequestHeader(value = "X-Auth-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    // --- GET: Single request by ID --------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Get a single request by ID")
    public ResponseEntity<RequestResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getById(id));
    }

    // --- PUT: Approve request (ADMIN) -----------------------------------------

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a request and deduct inventory (ADMIN only)")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        return ResponseEntity.ok(requestService.approveRequest(id));
    }

    // --- PUT: Reject request (ADMIN) ------------------------------------------

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a request with a comment (ADMIN only)")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Auth-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        String adminComment = (body != null) ? body.getOrDefault("adminComment", "") : "";
        return ResponseEntity.ok(requestService.rejectRequest(id, adminComment));
    }

    // --- GET: Filter by status (ADMIN) ----------------------------------------

    @GetMapping("/status/{status}")
    @Operation(summary = "Get requests by status (ADMIN only)")
    public ResponseEntity<?> getByStatus(
            @PathVariable String status,
            @RequestHeader(value = "X-Auth-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            StationeryRequest.Status statusEnum = StationeryRequest.Status.valueOf(status.toUpperCase());
            return ResponseEntity.ok(requestService.getByStatus(statusEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status +
                    ". Must be one of: PENDING, APPROVED, REJECTED, FULFILLED");
        }
    }
}
