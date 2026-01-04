package com.pizzaflow.booking.repository;

import com.pizzaflow.booking.domain.Booking;
import com.pizzaflow.booking.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(String customerId);
    List<Booking> findByTableIdAndBookingTimeBetween(Long tableId, Instant startTime, Instant endTime);
    List<Booking> findByStatus(BookingStatus status);
}
