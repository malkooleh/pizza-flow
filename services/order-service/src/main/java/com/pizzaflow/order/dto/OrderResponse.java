package com.pizzaflow.order.dto;

import com.pizzaflow.common.dto.Address;
import com.pizzaflow.order.domain.OrderStatus;
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
public class OrderResponse {
    private UUID id;
    private UUID customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;
    private Address deliveryAddress;
    private Double longitude;
    private Double latitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
