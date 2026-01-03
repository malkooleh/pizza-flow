package com.pizzaflow.payment.producer;

import com.pizzaflow.common.event.payment.PaymentEvent;
import com.pizzaflow.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_PAYMENT_COMPLETED = "payment.completed";
    private static final String TOPIC_PAYMENT_FAILED = "payment.failed";

    public void publishPaymentResult(Payment payment) {
        String topic = isSuccess(payment) ? TOPIC_PAYMENT_COMPLETED : TOPIC_PAYMENT_FAILED;

        PaymentEvent event = PaymentEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .timestamp(payment.getUpdatedAt() != null ? payment.getUpdatedAt() : payment.getCreatedAt())
                .build();

        log.info("Publishing Payment Event to topic: {} for Order ID: {}", topic, payment.getOrderId());
        kafkaTemplate.send(topic, String.valueOf(payment.getOrderId()), event);
    }

    private boolean isSuccess(Payment payment) {
        return "APPROVED".equals(payment.getStatus().name());
    }
}
