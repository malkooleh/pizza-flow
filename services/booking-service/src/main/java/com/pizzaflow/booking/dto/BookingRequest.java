package com.pizzaflow.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String customerId;
    private Integer tableNumber; // User might request a specific table, or we just assign one based on capacity
    private Integer partySize;
    private Instant bookingTime;
}
