package com.pizzaflow.delivery.consumer;

import com.pizzaflow.common.event.KitchenReadyEvent;
import com.pizzaflow.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenEventConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(topics = "kitchen.ready", groupId = "delivery-service-group")
    public void consumeKitchenReady(KitchenReadyEvent event) {
        log.info("Received KitchenReadyEvent for order: {}", event.getOrderId());
        try {
            deliveryService.assignCourier(event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to assign courier for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}
