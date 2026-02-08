package com.pizzaflow.kitchen.consumer;

import com.pizzaflow.common.event.OrderCancelledEvent;
import com.pizzaflow.kitchen.service.KitchenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledConsumer {

    private final KitchenService kitchenService;

    @KafkaListener(topics = "order.cancelled", groupId = "kitchen-service-group")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received Order Cancelled Event: Order ID = {}, Reason = {}", event.getOrderId(), event.getReason());
        try {
            kitchenService.cancelOrder(event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to cancel kitchen order {}", event.getOrderId(), e);
        }
    }
}
