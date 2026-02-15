package com.pizzaflow.delivery.dto;

import com.pizzaflow.common.dto.Address;
import com.pizzaflow.delivery.domain.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private Long id;
    private UUID orderId;
    private Long courierId;
    private String courierName;
    private DeliveryStatus status;
    private Address deliveryAddress;
    private Instant assignedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
}
