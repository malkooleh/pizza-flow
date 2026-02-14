package com.pizzaflow.common.event;

import com.pizzaflow.common.dto.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private UUID orderId;
    private Long customerId;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;
    private Address deliveryAddress;
    private Double longitude;
    private Double latitude;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private String productId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
