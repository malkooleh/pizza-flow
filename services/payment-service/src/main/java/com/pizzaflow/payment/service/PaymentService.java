package com.pizzaflow.payment.service;

import com.pizzaflow.payment.domain.Payment;
import com.pizzaflow.payment.domain.PaymentStatus;
import com.pizzaflow.payment.dto.PaymentRequest;
import com.pizzaflow.payment.dto.PaymentResponse;
import com.pizzaflow.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final com.pizzaflow.payment.producer.PaymentEventPublisher eventPublisher;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        // Save initial pending state
        payment = paymentRepository.save(payment);

        // Mock External Payment Gateway (Random Success/Fail)
        // In reality, this would be an HTTP call to Stripe/PayPal
        boolean success = Math.random() > 0.1; // 90% success rate

        if (success) {
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setTransactionId(UUID.randomUUID().toString());
            log.info("Payment approved for order: {}", request.getOrderId());
        } else {
            payment.setStatus(PaymentStatus.DECLINED);
            log.warn("Payment declined for order: {}", request.getOrderId());
        }

        Payment savedPayment = paymentRepository.save(payment);

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
