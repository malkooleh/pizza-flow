package com.pizzaflow.payment.service;

import com.pizzaflow.payment.domain.Payment;
import com.pizzaflow.payment.domain.PaymentStatus;
import com.pizzaflow.payment.dto.PaymentResponse;
import com.pizzaflow.payment.producer.PaymentEventPublisher;
import com.pizzaflow.payment.repository.PaymentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @Mock
    private ExternalPaymentGateway externalGateway;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    @Test
    void processPayment_ShouldApprove_WhenGatewayReturnsSuccess() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("25.00");

        when(externalGateway.processPayment(orderId, amount)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(100L);
            return p;
        });

        // Act
        PaymentResponse response = paymentService.processPayment(orderId, amount);

        // Assert
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.getId()).isEqualTo(100L);

        // Verify Repository
        verify(paymentRepository).save(any(Payment.class));

        // Verify Event
        verify(eventPublisher).publishPaymentResult(any(Payment.class));

        // Verify Metrics
        verify(meterRegistry).counter("pizzaflow.payments.result", "status", "approved");
    }

    @Test
    void processPayment_ShouldDecline_WhenGatewayReturnsFailure() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("25.00");

        when(externalGateway.processPayment(orderId, amount)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(100L);
            return p;
        });

        // Act
        PaymentResponse response = paymentService.processPayment(orderId, amount);

        // Assert
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.DECLINED);

        // Verify Metrics
        verify(meterRegistry).counter("pizzaflow.payments.result", "status", "declined");
    }
}
