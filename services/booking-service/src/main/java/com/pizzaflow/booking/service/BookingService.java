package com.pizzaflow.booking.service;

import com.pizzaflow.booking.domain.Booking;
import com.pizzaflow.booking.domain.BookingStatus;
import com.pizzaflow.booking.domain.RestaurantTable;
import com.pizzaflow.booking.dto.BookingRequest;
import com.pizzaflow.booking.dto.BookingResponse;
import com.pizzaflow.booking.repository.BookingRepository;
import com.pizzaflow.booking.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestaurantTableRepository tableRepository;

    private static final int DEFAULT_DURATION_HOURS = 2;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // 1. Find suitable table
        RestaurantTable table;
        if (request.getTableNumber() != null) {
            table = tableRepository.findByTableNumber(request.getTableNumber())
                    .orElseThrow(() -> new RuntimeException("Table " + request.getTableNumber() + " not found"));
            if (table.getCapacity() < request.getPartySize()) {
                throw new RuntimeException("Table capacity insufficient for party size " + request.getPartySize());
            }
        } else {
            // Find best fit: smallest capacity that is >= partySize
            List<RestaurantTable> allTables = tableRepository.findAll();
            table = allTables.stream()
                    .filter(t -> t.getCapacity() >= request.getPartySize())
                    .min(Comparator.comparingInt(RestaurantTable::getCapacity))
                    .orElseThrow(() -> new RuntimeException("No suitable table found for party size " + request.getPartySize()));
        }

        // 2. Check availability
        Instant start = request.getBookingTime();
        Instant end = start.plus(DEFAULT_DURATION_HOURS, ChronoUnit.HOURS);
        
        List<Booking> overlapping = bookingRepository.findByTableIdAndBookingTimeBetween(table.getId(), start.minus(DEFAULT_DURATION_HOURS, ChronoUnit.HOURS), end);
        // Note: The repository method 'findByTableIdAndBookingTimeBetween' is likely not sufficient for full overlap check without a custom query, 
        // but for MVP, assuming 'BookingTime' is the start time, we check if any booking starts within the conflict window.
        // A better query would be: WHERE table_id = ? AND (start_time < ? AND end_time > ?)
        // For now, let's keep it simple. If any booking exists for this table +/- 2 hours.
        
        // Actually, let's just use a simple overlap logic in memory for now if the volume is low, or refine the query later.
        // We will query bookings for the table around the time.
        
        boolean conflict = overlapping.stream().anyMatch(b -> {
             if (b.getStatus() == BookingStatus.CANCELLED) return false;
             Instant bStart = b.getBookingTime();
             Instant bEnd = bStart.plus(DEFAULT_DURATION_HOURS, ChronoUnit.HOURS);
             return start.isBefore(bEnd) && end.isAfter(bStart); // Overlap condition
        });

        if (conflict) {
            throw new RuntimeException("Table is already booked for the selected time");
        }

        // 3. Create Booking
        Booking booking = new Booking();
        booking.setCustomerId(request.getCustomerId());
        booking.setTable(table);
        booking.setBookingTime(request.getBookingTime());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    public List<BookingResponse> getBookingsForCustomer(String customerId) {
        return bookingRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    private BookingResponse mapToResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getCustomerId(),
                booking.getTable().getTableNumber(),
                booking.getBookingTime(),
                booking.getStatus()
        );
    }
}
