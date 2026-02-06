package com.pizzaflow.delivery.service;

import com.pizzaflow.common.event.DeliveryAssignedEvent;
import com.pizzaflow.delivery.domain.*;
import com.pizzaflow.delivery.repository.CourierRepository;
import com.pizzaflow.delivery.repository.DeliveryRepository;
import com.pizzaflow.delivery.repository.DeliveryZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private DeliveryZoneRepository deliveryZoneRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private DeliveryService deliveryService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void createDelivery_ShouldSaveDelivery_WhenAddressInZone() {
        // Arrange
        Long orderId = 1L;
        String address = "123 Main St";
        double lon = 10.0;
        double lat = 20.0;
        // Point location = geometryFactory.createPoint(new Coordinate(lon, lat)); //
        // Unused explicitly in simplified test

        // Mock Zone check to succeed
        when(deliveryZoneRepository.findZoneForPoint(any(Point.class))).thenReturn(Optional.of(new DeliveryZone()));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Delivery result = deliveryService.createDelivery(orderId, address, lon, lat);

        // Assert
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(result.getDeliveryLocation().getCoordinate().x).isEqualTo(lon);
        assertThat(result.getDeliveryLocation().getCoordinate().y).isEqualTo(lat);

        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    void assignCourier_ShouldAssignNearestCourier_WhenAvailable() {
        // Arrange
        Long orderId = 1L;
        Point location = geometryFactory.createPoint(new Coordinate(10.0, 20.0));

        Delivery delivery = Delivery.builder()
                .id(100L)
                .orderId(orderId)
                .deliveryLocation(location)
                .status(DeliveryStatus.PENDING)
                .build();

        Courier courier = Courier.builder()
                .id(200L)
                .name("John Doe")
                .status(CourierStatus.AVAILABLE)
                .build();

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));
        when(courierRepository.findNearestAvailableCourier(any(Point.class))).thenReturn(courier);

        // Act
        deliveryService.assignCourier(orderId);

        // Assert
        // 1. Verify Courier Status Update
        assertThat(courier.getStatus()).isEqualTo(CourierStatus.BUSY);
        verify(courierRepository).save(courier);

        // 2. Verify Delivery Update
        assertThat(delivery.getCourier()).isEqualTo(courier);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
        verify(deliveryRepository).save(delivery);

        // 3. Verify Kafka Event
        verify(kafkaTemplate).send(eq("delivery.assigned"), eq("1"), any(DeliveryAssignedEvent.class));
    }

    @Test
    void updateDeliveryStatus_ShouldFreeCourier_WhenDelivered() {
        // Arrange
        Long orderId = 1L;
        Courier courier = Courier.builder().id(200L).status(CourierStatus.BUSY).build();
        Delivery delivery = Delivery.builder()
                .id(100L)
                .orderId(orderId)
                .courier(courier)
                .status(DeliveryStatus.PICKED_UP)
                .build();

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));

        // Act
        deliveryService.updateDeliveryStatus(orderId, DeliveryStatus.DELIVERED);

        // Assert
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getDeliveredAt()).isNotNull();

        // Check Courier is now AVAILABLE
        assertThat(courier.getStatus()).isEqualTo(CourierStatus.AVAILABLE);
        verify(courierRepository).save(courier);

        verify(deliveryRepository).save(delivery);
    }
}
