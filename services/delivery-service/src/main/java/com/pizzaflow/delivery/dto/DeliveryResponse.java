package com.pizzaflow.delivery.dto;

import com.pizzaflow.delivery.domain.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private Long id;
    private Long orderId;
    private Long courierId;
    private String courierName;
    private DeliveryStatus status;
    private String deliveryAddress;
    private Instant assignedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
}
