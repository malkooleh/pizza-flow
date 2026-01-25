package com.pizzaflow.delivery.consumer;

import com.pizzaflow.common.event.OrderCreatedEvent;
import com.pizzaflow.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(topics = "order.created", groupId = "delivery-service-group")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order: {}", event.getOrderId());
        try {
            if (event.getDeliveryAddress() != null && event.getLongitude() != null && event.getLatitude() != null) {
                deliveryService.createDelivery(
                        event.getOrderId(),
                        event.getDeliveryAddress(),
                        event.getLongitude(),
                        event.getLatitude()
                );
                log.info("Created PENDING delivery for order: {}", event.getOrderId());
            } else {
                log.warn("Order {} missing delivery information. Skipping delivery creation.", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to create delivery for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}
