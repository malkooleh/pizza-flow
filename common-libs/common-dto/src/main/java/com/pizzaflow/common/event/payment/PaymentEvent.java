package com.pizzaflow.common.event.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private Long paymentId;
    private Long orderId;
    private String status; // "APPROVED", "DECLINED", "FAILED"
    private BigDecimal amount;
    private LocalDateTime timestamp;
}
