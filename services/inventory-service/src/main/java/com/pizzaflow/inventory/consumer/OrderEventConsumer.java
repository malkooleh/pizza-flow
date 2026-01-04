package com.pizzaflow.inventory.consumer;

import com.pizzaflow.common.event.OrderCreatedEvent;
import com.pizzaflow.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "order.created", groupId = "inventory-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received Order Created Event: Order ID = {}", event.getOrderId());

        try {
            // Map product IDs to quantities
            Map<String, Integer> productQuantities = event.getItems().stream()
                    .collect(Collectors.toMap(
                            OrderCreatedEvent.OrderItemEvent::getProductId,
                            OrderCreatedEvent.OrderItemEvent::getQuantity,
                            Integer::sum));

            inventoryService.reserveStockForOrder(event.getOrderId(), productQuantities);

        } catch (Exception e) {
            log.error("Failed to process order created event for order {}", event.getOrderId(), e);
            // Event will be published via Outbox in InventoryService
        }
    }
}
