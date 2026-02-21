package com.pizzaflow.order.controller;

import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.dto.OrderResponse;
import com.pizzaflow.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse createdOrder = orderService.createOrder(request);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + createdOrder.getId()))
                .body(createdOrder);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderResponse(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id,
            @RequestBody java.util.Map<String, String> payload) {
        String statusStr = payload.get("status");
        com.pizzaflow.order.domain.OrderStatus status = com.pizzaflow.order.domain.OrderStatus.valueOf(statusStr);
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@PathVariable UUID customerId) {
        return ResponseEntity.ok(orderService.getOrdersResponseByCustomer(customerId));
    }
}
