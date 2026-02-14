package com.pizzaflow.payment.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class ExternalPaymentGateway {

    private final Random random = new Random();

    @Retry(name = "externalPayment", fallbackMethod = "processPaymentFallback")
    @Bulkhead(name = "externalPayment", type = Bulkhead.Type.SEMAPHORE)
    public boolean processPayment(UUID orderId, java.math.BigDecimal amount) {
        log.info("Contacting External Payment Provider for order: {}", orderId);

        // Deterministic failure for E2E testing
        if (amount.compareTo(new java.math.BigDecimal("0.99")) == 0) {
            log.warn("Mocking Payment Decline for E2E Test (Amount 0.99)");
            return false;
        }

        // Simulate random network delay
        try {
            Thread.sleep(random.nextInt(100, 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate 20% random failure
        if (random.nextInt(10) < 2) {
            log.warn("External Payment Provider connection failed for order: {}", orderId);
            throw new RuntimeException("External gateway unavailable");
        }

        log.info("External Payment Provider approved for order: {}", orderId);
        return true;
    }

    public boolean processPaymentFallback(UUID orderId, java.math.BigDecimal amount, Throwable t) {
        log.error("All retries failed for transaction {}. Reason: {}", orderId, t.getMessage());
        return false; // Transaction failed after retries
    }
}
