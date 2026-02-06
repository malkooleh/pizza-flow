package com.pizzaflow.booking.service;

import com.pizzaflow.booking.domain.Booking;
import com.pizzaflow.booking.domain.BookingStatus;
import com.pizzaflow.booking.domain.RestaurantTable;
import com.pizzaflow.booking.dto.BookingRequest;
import com.pizzaflow.booking.dto.BookingResponse;
import com.pizzaflow.booking.repository.BookingRepository;
import com.pizzaflow.booking.repository.RestaurantTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_ShouldSuccess_WhenTableAvailable() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setCustomerId("cust-1");
        request.setPartySize(4);
        request.setBookingTime(Instant.now().plus(1, ChronoUnit.DAYS));

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setTableNumber(101);
        table.setCapacity(4);

        when(tableRepository.findAll()).thenReturn(List.of(table));
        when(bookingRepository.findByTableIdAndBookingTimeBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(500L);
            return b;
        });

        // Act
        BookingResponse response = bookingService.createBooking(request);

        // Assert
        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.getTableNumber()).isEqualTo(101);
    }

    @Test
    void createBooking_ShouldFail_WhenNoTableCapacity() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setPartySize(10); // Too big

        RestaurantTable table = new RestaurantTable();
        table.setCapacity(4);

        when(tableRepository.findAll()).thenReturn(List.of(table));

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No suitable table found");
    }
}
