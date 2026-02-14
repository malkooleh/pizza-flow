package com.pizzaflow.common.event;

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
public class KitchenReadyEvent {
    private UUID orderId;
    private Instant readyAt;
    // For geospatial lookup, we might need the restaurant location or delivery
    // address
    // But for now, just the order ID. Delivery service will have the address from
    // the order or a separate call.
}
