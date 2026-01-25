package com.pizzaflow.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAssignedEvent {
    private Long orderId;
    private Long courierId;
    private String courierName;
    private Instant assignedAt;
}
