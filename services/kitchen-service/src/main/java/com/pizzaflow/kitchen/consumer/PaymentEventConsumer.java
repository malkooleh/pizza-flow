package com.pizzaflow.kitchen.consumer;

import com.pizzaflow.common.event.payment.PaymentEvent;
import com.pizzaflow.kitchen.service.KitchenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final KitchenService kitchenService;

    @KafkaListener(topics = "payment.completed", groupId = "kitchen-service-group")
    public void handlePaymentCompleted(PaymentEvent event) {
        log.info("Received Payment Completed Event: {}", event);
        kitchenService.processPaymentEvent(event);
    }
}
