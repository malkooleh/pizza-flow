package com.pizzaflow.payment.service;

import com.pizzaflow.payment.domain.Payment;
import com.pizzaflow.payment.domain.PaymentStatus;
import com.pizzaflow.payment.dto.PaymentResponse;
import com.pizzaflow.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.pizzaflow.payment.producer.PaymentEventPublisher;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;
    private final ExternalPaymentGateway externalGateway;

    @Transactional
    public PaymentResponse processPayment(Long orderId, java.math.BigDecimal amount) {
        log.info("Processing payment for order: {} amount: {}", orderId, amount);

        // Delegate to resilient external gateway
        boolean success = externalGateway.processPayment(orderId, amount);

        PaymentStatus status = success ? PaymentStatus.APPROVED : PaymentStatus.DECLINED;

        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .status(status)
                .transactionId("TXN-" + System.currentTimeMillis())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (success) {
            log.info("Payment approved for order: {}", orderId);
        } else {
            log.warn("Payment declined for order: {}", orderId);
        }

        // Publish Event
        eventPublisher.publishPaymentResult(savedPayment);

        return mapToResponse(savedPayment);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
