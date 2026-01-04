package com.pizzaflow.booking.controller;

import com.pizzaflow.booking.domain.RestaurantTable;
import com.pizzaflow.booking.dto.BookingRequest;
import com.pizzaflow.booking.dto.BookingResponse;
import com.pizzaflow.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BookingResponse>> getBookings(@PathVariable String customerId) {
        return ResponseEntity.ok(bookingService.getBookingsForCustomer(customerId));
    }
    
    @GetMapping("/tables")
    public ResponseEntity<List<RestaurantTable>> getTables() {
        return ResponseEntity.ok(bookingService.getAllTables());
    }
}
