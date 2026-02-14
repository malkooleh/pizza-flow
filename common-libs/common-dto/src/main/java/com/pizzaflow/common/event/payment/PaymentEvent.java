package com.pizzaflow.common.event.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private Long paymentId;
    private UUID orderId;
    private String status; // "APPROVED", "DECLINED", "FAILED"
    private BigDecimal amount;
    private LocalDateTime timestamp;
}
