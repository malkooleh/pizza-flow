package com.pizzaflow.kitchen.controller;

import com.pizzaflow.kitchen.domain.KitchenStatus;
import com.pizzaflow.kitchen.dto.KitchenOrderDto;
import com.pizzaflow.kitchen.service.KitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenService kitchenService;

    @GetMapping("/queue")
    public ResponseEntity<List<KitchenOrderDto>> getQueue() {
        return ResponseEntity.ok(kitchenService.getActiveQueue());
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<KitchenOrderDto> updateStatus(@PathVariable Long orderId, @RequestParam KitchenStatus status) {
        return ResponseEntity.ok(kitchenService.updateStatus(orderId, status));
    }
}
