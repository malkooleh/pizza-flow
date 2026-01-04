package com.pizzaflow.inventory.controller;

import com.pizzaflow.inventory.dto.CreateInventoryItemRequest;
import com.pizzaflow.inventory.dto.InventoryItemResponse;
import com.pizzaflow.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryItemResponse> createInventoryItem(@Valid @RequestBody CreateInventoryItemRequest request) {
        InventoryItemResponse response = inventoryService.createInventoryItem(request);
        return ResponseEntity.created(URI.create("/api/v1/inventory/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemResponse> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getItemById(id));
    }
}
