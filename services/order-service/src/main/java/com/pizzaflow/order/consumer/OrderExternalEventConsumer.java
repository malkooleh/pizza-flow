package com.pizzaflow.order.consumer;

import com.pizzaflow.common.event.KitchenReadyEvent;
import com.pizzaflow.common.event.DeliveryAssignedEvent;
import com.pizzaflow.order.domain.OrderStatus;
import com.pizzaflow.order.service.OrderService;
import com.pizzaflow.order.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExternalEventConsumer {

    private final SseService sseService;
    private final OrderService orderService;

    @KafkaListener(topics = "kitchen.ready", groupId = "order-service-sse-group")
    public void handleKitchenReady(KitchenReadyEvent event) {
        log.info("Received KitchenReadyEvent for Order ID: {}", event.getOrderId());
        
        // 1. Update backend state
        orderService.updateStatus(event.getOrderId(), OrderStatus.READY_FOR_DELIVERY);
        
        // 2. Broadcast to SSE clients
        sseService.broadcastUpdate(event.getOrderId(), Map.of(
            "orderId", event.getOrderId(),
            "status", "READY_FOR_DELIVERY",
            "message", "Order is ready for delivery!"
        ));
    }

    @KafkaListener(topics = "delivery.assigned", groupId = "order-service-sse-group")
    public void handleDeliveryAssigned(DeliveryAssignedEvent event) {
        log.info("Received DeliveryAssignedEvent for Order ID: {}", event.getOrderId());
        
        // 1. Update backend state
        orderService.updateStatus(event.getOrderId(), OrderStatus.OUT_FOR_DELIVERY);
        
        // 2. Broadcast to SSE clients
        sseService.broadcastUpdate(event.getOrderId(), Map.of(
            "orderId", event.getOrderId(),
            "status", "OUT_FOR_DELIVERY",
            "message", "Courier is on the way!"
        ));
    }
}
