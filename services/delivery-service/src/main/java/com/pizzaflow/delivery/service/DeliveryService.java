package com.pizzaflow.delivery.service;

import com.pizzaflow.common.dto.Address;
import com.pizzaflow.common.event.DeliveryAssignedEvent;
import com.pizzaflow.delivery.domain.Courier;
import com.pizzaflow.delivery.domain.CourierStatus;
import com.pizzaflow.delivery.domain.Delivery;
import com.pizzaflow.delivery.domain.DeliveryStatus;
import com.pizzaflow.delivery.repository.CourierRepository;
import com.pizzaflow.delivery.repository.DeliveryRepository;
import com.pizzaflow.delivery.repository.DeliveryZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public Delivery createDelivery(UUID orderId, Address address, double lon, double lat) {
        Point location = geometryFactory.createPoint(new Coordinate(lon, lat));

        // Check if within delivery zone
        deliveryZoneRepository.findZoneForPoint(location)
                .orElseThrow(() -> new RuntimeException("Address is outside delivery zones"));

        Delivery delivery = Delivery.builder()
                .orderId(orderId)
                .deliveryAddress(address)
                .deliveryLocation(location)
                .status(DeliveryStatus.PENDING)
                .build();

        return deliveryRepository.save(delivery);
    }

    @Transactional
    public void assignCourier(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order: " + orderId));

        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            log.warn("Delivery for order {} is already {}", orderId, delivery.getStatus());
            return;
        }

        Courier nearestCourier = courierRepository.findNearestAvailableCourier(delivery.getDeliveryLocation());

        if (nearestCourier != null) {
            log.info("Assigning courier {} to delivery for order {}", nearestCourier.getName(), orderId);
            nearestCourier.setStatus(CourierStatus.BUSY);
            courierRepository.save(nearestCourier);

            delivery.setCourier(nearestCourier);
            delivery.setStatus(DeliveryStatus.ASSIGNED);
            delivery.setAssignedAt(Instant.now());
            deliveryRepository.save(delivery);

            log.info("Emitting DeliveryAssignedEvent for order {}", orderId);
            DeliveryAssignedEvent event = DeliveryAssignedEvent.builder()
                    .orderId(orderId)
                    .courierId(nearestCourier.getId())
                    .courierName(nearestCourier.getName())
                    .assignedAt(delivery.getAssignedAt())
                    .build();
            kafkaTemplate.send("delivery.assigned", orderId.toString(), event);
        } else {
            log.warn("No available couriers for delivery of order {}", orderId);
            // In a real system, we might retry or queue this
        }
    }

    @Transactional
    public Courier registerCourier(String name, double lon, double lat) {
        Point location = geometryFactory.createPoint(new Coordinate(lon, lat));
        Courier courier = Courier.builder()
                .name(name)
                .status(CourierStatus.AVAILABLE)
                .currentLocation(location)
                .build();
        return courierRepository.save(courier);
    }

    @Transactional
    public void updateCourierLocation(Long courierId, double lon, double lat) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found: " + courierId));

        Point location = geometryFactory.createPoint(new Coordinate(lon, lat));
        courier.setCurrentLocation(location);
        courierRepository.save(courier);
    }

    @Transactional
    public void updateDeliveryStatus(UUID orderId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order: " + orderId));

        delivery.setStatus(status);
        if (status == DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(Instant.now());
        } else if (status == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(Instant.now());
            if (delivery.getCourier() != null) {
                delivery.getCourier().setStatus(CourierStatus.AVAILABLE);
                courierRepository.save(delivery.getCourier());
            }
        }
        deliveryRepository.save(delivery);
    }

    public Delivery getDelivery(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order: " + orderId));
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }
}
