package com.pizzaflow.payment.consumer;

import com.pizzaflow.common.event.OrderCreatedEvent;
import com.pizzaflow.payment.dto.PaymentRequest;
import com.pizzaflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order.created", groupId = "payment-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for Order ID: {}", event.getOrderId());
        
        PaymentRequest request = PaymentRequest.builder()
                .orderId(event.getOrderId())
                .amount(event.getTotalAmount())
                .build();
        
        paymentService.processPayment(request);
    }
}
