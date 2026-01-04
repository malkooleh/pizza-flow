package com.pizzaflow.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.inventory.domain.*;
import com.pizzaflow.inventory.dto.CreateInventoryItemRequest;
import com.pizzaflow.inventory.dto.InventoryItemResponse;
import com.pizzaflow.inventory.exception.InsufficientStockException;
import com.pizzaflow.inventory.exception.ResourceNotFoundException;
import com.pizzaflow.inventory.repository.InventoryItemRepository;
import com.pizzaflow.inventory.repository.OutboxEventRepository;
import com.pizzaflow.inventory.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockReservationRepository stockReservationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public InventoryItemResponse createInventoryItem(CreateInventoryItemRequest request) {
        InventoryItem item = InventoryItem.builder()
                .productId(request.getProductId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .unit(request.getUnit())
                .build();

        InventoryItem savedItem = inventoryItemRepository.save(item);
        log.info("Created inventory item: {}", savedItem.getProductId());

        return mapToResponse(savedItem);
    }

    public List<InventoryItemResponse> getAllItems() {
        return inventoryItemRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public InventoryItemResponse getItemById(Long id) {
        InventoryItem item = findItemById(id);
        return mapToResponse(item);
    }

    @Transactional
    public void reserveStockForOrder(Long orderId, Map<String, Integer> productQuantities) {
        log.info("Attempting to reserve stock for order: {}", orderId);

        // Check if stock is already reserved
        List<StockReservation> existingReservations = stockReservationRepository.findByOrderId(orderId);
        if (!existingReservations.isEmpty()) {
            log.warn("Stock already reserved for order: {}", orderId);
            return;
        }

        try {
            // Reserve stock for each product
            for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
                String productId = entry.getKey();
                Integer quantity = entry.getValue();

                InventoryItem item = inventoryItemRepository.findByProductId(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

                // Check availability and reserve
                if (!item.canReserve(quantity)) {
                    throw new InsufficientStockException(
                            "Insufficient stock for product: " + productId +
                                    ". Available: " + item.getAvailableQuantity() +
                                    ", Requested: " + quantity);
                }

                item.reserve(quantity);
                inventoryItemRepository.save(item);

                // Create reservation record
                StockReservation reservation = StockReservation.builder()
                        .orderId(orderId)
                        .inventoryItem(item)
                        .quantity(quantity)
                        .status(ReservationStatus.RESERVED)
                        .build();
                stockReservationRepository.save(reservation);

                log.info("Reserved {} units of {} for order {}", quantity, productId, orderId);
            }

            // Publish success event via Outbox
            publishInventoryEvent(orderId.toString(), "INVENTORY_RESERVED", Map.of(
                    "orderId", orderId,
                    "status", "RESERVED",
                    "products", productQuantities));

        } catch (InsufficientStockException | ResourceNotFoundException e) {
            log.error("Failed to reserve stock for order {}: {}", orderId, e.getMessage());

            // Publish failure event via Outbox
            publishInventoryEvent(orderId.toString(), "INVENTORY_UNAVAILABLE", Map.of(
                    "orderId", orderId,
                    "status", "UNAVAILABLE",
                    "reason", e.getMessage()));

            throw e;
        }
    }

    /**
     * Release reserved stock for a cancelled or failed order.
     * This method is called when an order is cancelled before fulfillment,
     * returning the reserved quantity back to available stock.
     *
     * FUTURE INTEGRATION POINTS:
     * - Kafka Consumer listening to 'order.cancelled' events
     * - Payment failure compensation in the Saga pattern
     * - Order timeout scheduler (e.g., unpaid orders after 15 minutes)
     *
     * @param orderId The ID of the order to release stock for
     */
    @Transactional
    public void releaseStockForOrder(Long orderId) {
        log.info("Releasing stock for order: {}", orderId);

        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                InventoryItem item = reservation.getInventoryItem();
                item.release(reservation.getQuantity());
                inventoryItemRepository.save(item);

                reservation.setStatus(ReservationStatus.RELEASED);
                stockReservationRepository.save(reservation);

                log.info("Released {} units of {} for order {}",
                        reservation.getQuantity(),
                        item.getProductId(),
                        orderId);
            }
        }
    }

    /**
     * Commit reserved stock for a completed order.
     * This method finalizes the stock deduction, moving quantity from 'reserved' to
     * 'committed',
     * and reducing the total quantity. Called when an order is successfully
     * delivered.
     *
     * FUTURE INTEGRATION POINTS:
     * - Kafka Consumer listening to 'delivery.completed' events
     * - Kitchen completion handler (for pickup orders)
     * - Final stage of the Order Saga pattern
     *
     * @param orderId The ID of the order to commit stock for
     */
    @Transactional
    public void commitStockForOrder(Long orderId) {
        log.info("Committing stock for order: {}", orderId);

        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                InventoryItem item = reservation.getInventoryItem();
                item.commit(reservation.getQuantity());
                inventoryItemRepository.save(item);

                reservation.setStatus(ReservationStatus.CONFIRMED);
                stockReservationRepository.save(reservation);

                log.info("Committed {} units of {} for order {}",
                        reservation.getQuantity(),
                        item.getProductId(),
                        orderId);
            }
        }
    }

    private void publishInventoryEvent(String aggregateId, String eventType, Map<String, Object> payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent event = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .aggregateType("INVENTORY")
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(event);
            log.info("Created outbox event: {} for aggregate: {}", eventType, aggregateId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload", e);
        }
    }

    private InventoryItem findItemById(Long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));
    }

    private InventoryItemResponse mapToResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .availableQuantity(item.getAvailableQuantity())
                .unit(item.getUnit())
                .build();
    }
}
