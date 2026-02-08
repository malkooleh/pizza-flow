package com.pizzaflow.order.producer;

import com.pizzaflow.common.event.OrderCancelledEvent;
import com.pizzaflow.common.event.OrderCreatedEvent;
import com.pizzaflow.order.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_ORDER_CREATED = "order.created";

    public void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(item -> OrderCreatedEvent.OrderItemEvent.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .build())
                        .toList())
                .deliveryAddress(order.getDeliveryAddress())
                .longitude(order.getLongitude())
                .latitude(order.getLatitude())
                .build();

        log.info("Publishing OrderCreatedEvent for Order ID: {}", order.getId());
        kafkaTemplate.send(TOPIC_ORDER_CREATED, String.valueOf(order.getId()), event);
    }

    public void publishOrderCancelledEvent(Long orderId, String reason) {
        OrderCancelledEvent event = OrderCancelledEvent
                .builder()
                .orderId(orderId)
                .reason(reason)
                .cancelledAt(java.time.LocalDateTime.now())
                .build();

        log.info("Publishing OrderCancelledEvent for Order ID: {}", orderId);
        kafkaTemplate.send("order.cancelled", String.valueOf(orderId), event);
    }
}
