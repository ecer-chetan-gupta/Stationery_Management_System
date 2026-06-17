package com.sms.request.feign;

import com.sms.request.model.dto.ItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Inventory Service.
 * The name "inventory-service" MUST match spring.application.name in inventory-service.
 */
@FeignClient(name = "inventory-service")
public interface InventoryFeignClient {

    @GetMapping("/api/inventory/{id}")
    ItemResponse getItemById(@PathVariable("id") Long id);

    @PutMapping("/api/inventory/{id}/deduct")
    void deductQuantity(@PathVariable("id") Long id, @RequestParam("qty") int qty);
}
