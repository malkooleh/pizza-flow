package com.pizzaflow.delivery.controller;

import com.pizzaflow.delivery.domain.Delivery;
import com.pizzaflow.delivery.domain.Courier;
import com.pizzaflow.delivery.domain.DeliveryStatus;
import com.pizzaflow.delivery.dto.CourierRequest;
import com.pizzaflow.delivery.dto.CourierResponse;
import com.pizzaflow.delivery.dto.DeliveryResponse;
import com.pizzaflow.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/couriers")
    public ResponseEntity<CourierResponse> registerCourier(@RequestBody CourierRequest request) {
        Courier courier = deliveryService
                .registerCourier(request.getName(), request.getLongitude(), request.getLatitude());
        return ResponseEntity.ok(mapToCourierResponse(courier));
    }

    @PatchMapping("/couriers/{id}/location")
    public ResponseEntity<Void> updateCourierLocation(
            @PathVariable Long id,
            @RequestParam double lon,
            @RequestParam double lat) {
        deliveryService.updateCourierLocation(id, lon, lat);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable UUID orderId) {
        Delivery delivery = deliveryService.getDelivery(orderId);
        return ResponseEntity.ok(mapToDeliveryResponse(delivery));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateDeliveryStatus(
            @PathVariable UUID orderId,
            @RequestParam DeliveryStatus status) {
        deliveryService.updateDeliveryStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    private DeliveryResponse mapToDeliveryResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .courierId(delivery.getCourier() != null ? delivery.getCourier().getId() : null)
                .courierName(delivery.getCourier() != null ? delivery.getCourier().getName() : null)
                .status(delivery.getStatus())
                .deliveryAddress(delivery.getDeliveryAddress())
                .assignedAt(delivery.getAssignedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .deliveredAt(delivery.getDeliveredAt())
                .build();
    }

    private CourierResponse mapToCourierResponse(Courier courier) {
        CourierResponse response = new CourierResponse();
        response.setId(courier.getId());
        response.setName(courier.getName());
        response.setStatus(courier.getStatus());
        if (courier.getCurrentLocation() != null) {
            response.setLongitude(courier.getCurrentLocation().getX());
            response.setLatitude(courier.getCurrentLocation().getY());
        }
        return response;
    }
}
