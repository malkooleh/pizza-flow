package com.pizzaflow.kitchen.consumer;

import com.pizzaflow.common.event.OrderCreatedEvent;
import com.pizzaflow.common.event.DeliveryAssignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "order.created", groupId = "kitchen-service-ws-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received Order Created Event for WebSocket broadcast: {}", event.getOrderId());
        messagingTemplate.convertAndSend("/topic/orders", java.util.Map.of(
            "orderId", event.getOrderId(),
            "status", "PENDING",
            "type", "ORDER_CREATED"
        ));
    }

    @KafkaListener(topics = "delivery.assigned", groupId = "kitchen-service-ws-group")
    public void handleDeliveryAssigned(DeliveryAssignedEvent event) {
        log.info("Received Delivery Assigned Event for WebSocket broadcast: {}", event.getOrderId());
        messagingTemplate.convertAndSend("/topic/orders", java.util.Map.of(
            "orderId", event.getOrderId(),
            "status", "OUT_FOR_DELIVERY",
            "type", "DELIVERY_ASSIGNED"
        ));
        messagingTemplate.convertAndSend("/topic/orders/" + event.getOrderId(), java.util.Map.of(
            "orderId", event.getOrderId(),
            "status", "OUT_FOR_DELIVERY"
        ));
    }
}
