package com.pizzaflow.order.consumer;

import com.pizzaflow.common.event.payment.PaymentEvent;
import com.pizzaflow.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "payment.completed", groupId = "order-service")
    public void handlePaymentCompleted(PaymentEvent event) {
        log.info("Received Payment Completed event for Order ID: {}", event.getOrderId());
        try {
            orderService.processPaymentSuccess(event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing payment success for order {}", event.getOrderId(), e);
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service")
    public void handlePaymentFailed(PaymentEvent event) {
        log.info("Received Payment Failed event for Order ID: {}", event.getOrderId());
        try {
            orderService.processPaymentFailure(event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing payment failure for order {}", event.getOrderId(), e);
        }
    }
}
