package com.pizzaflow.order.controller;

import com.pizzaflow.order.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderTrackingController {

    private final SseService sseService;

    @GetMapping("/{orderId}/track")
    public SseEmitter trackOrder(@PathVariable UUID orderId) {
        log.info("Client requested tracking for order: {}", orderId);
        return sseService.createEmitter(orderId);
    }
}
