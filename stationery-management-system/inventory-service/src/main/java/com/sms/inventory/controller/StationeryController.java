package com.sms.inventory.controller;

import com.sms.inventory.model.StationeryItem;
import com.sms.inventory.model.dto.ItemRequest;
import com.sms.inventory.model.dto.ItemResponse;
import com.sms.inventory.service.StationeryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Inventory REST Controller
 * Base path: /api/inventory
 *
 * Role enforcement:
 *   - GET endpoints: any authenticated user (enforced at Gateway)
 *   - POST/PUT/DELETE: ADMIN only — Gateway passes X-Auth-User-Role header;
 *     we check it manually since Gateway handles JWT and we permit all at Spring Security level.
 */
@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Stationery inventory CRUD and stock management")
public class StationeryController {

    private static final Logger log = LoggerFactory.getLogger(StationeryController.class);

    private final StationeryService stationeryService;

    public StationeryController(StationeryService stationeryService) {
        this.stationeryService = stationeryService;
    }

    // --- GET: List all items (paginated) --------------------------------------

    @GetMapping
    @Operation(summary = "Get all stationery items (paginated)")
    public ResponseEntity<Page<ItemResponse>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) StationeryItem.Category category) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ItemResponse> result = (category != null)
                ? stationeryService.getByCategory(category, pageable)
                : stationeryService.getAllItems(pageable);
        return ResponseEntity.ok(result);
    }

    // --- GET: Single item -----------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Get a stationery item by ID")
    public ResponseEntity<ItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(stationeryService.getById(id));
    }

    // --- GET: Search by name --------------------------------------------------

    @GetMapping("/search")
    @Operation(summary = "Search stationery items by name")
    public ResponseEntity<List<ItemResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(stationeryService.searchItems(q));
    }

    // --- GET: Low-stock alert (ADMIN) -----------------------------------------

    @GetMapping("/low-stock")
    @Operation(summary = "Get low-stock items (ADMIN only)")
    public ResponseEntity<?> getLowStock(@RequestHeader(value = "X-Auth-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        return ResponseEntity.ok(stationeryService.getLowStockItems());
    }

    // --- POST: Create item (ADMIN) --------------------------------------------

    @PostMapping
    @Operation(summary = "Add a new stationery item (ADMIN only)")
    public ResponseEntity<?> addItem(
            @Valid @RequestBody ItemRequest request,
            @RequestHeader(value = "X-Auth-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        ItemResponse response = stationeryService.addItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- PUT: Update item (ADMIN) ---------------------------------------------

    @PutMapping("/{id}")
    @Operation(summary = "Update a stationery item (ADMIN only)")
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request,
            @RequestHeader(value = "X-Auth-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        return ResponseEntity.ok(stationeryService.updateItem(id, request));
    }

    // --- DELETE: Delete item (ADMIN) ------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a stationery item (ADMIN only)")
    public ResponseEntity<?> deleteItem(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        stationeryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    // --- PUT: Deduct stock (internal Feign call from Request Service) ----------

    @PutMapping("/{id}/deduct")
    @Operation(summary = "Deduct stock quantity (internal Feign call — no role check)")
    public ResponseEntity<Void> deductQuantity(
            @PathVariable Long id,
            @RequestParam int qty) {
        stationeryService.deductQuantity(id, qty);
        return ResponseEntity.ok().build();
    }
}
