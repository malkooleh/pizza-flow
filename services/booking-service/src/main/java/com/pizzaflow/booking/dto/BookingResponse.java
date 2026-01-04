package com.pizzaflow.booking.dto;

import com.pizzaflow.booking.domain.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String customerId;
    private Integer tableNumber;
    private Instant bookingTime;
    private BookingStatus status;
}
