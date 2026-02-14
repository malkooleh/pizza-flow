package com.pizzaflow.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.inventory.domain.InventoryItem;
import com.pizzaflow.inventory.domain.OutboxEvent;
import com.pizzaflow.inventory.domain.OutboxStatus;
import com.pizzaflow.inventory.domain.StockReservation;
import com.pizzaflow.inventory.exception.InsufficientStockException;
import com.pizzaflow.inventory.repository.InventoryItemRepository;
import com.pizzaflow.inventory.repository.OutboxEventRepository;
import com.pizzaflow.inventory.repository.StockReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void reserveStockForOrder_ShouldReserveStockAndPublishEvent_WhenStockAvailable() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String productId = "pizza-1";
        Map<String, Integer> productQuantities = Map.of(productId, 2);

        InventoryItem item = InventoryItem.builder()
                .productId(productId)
                .quantity(10)
                .reservedQuantity(0)
                .build();

        when(stockReservationRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());
        when(inventoryItemRepository.findByProductId(productId)).thenReturn(Optional.of(item));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        inventoryService.reserveStockForOrder(orderId, productQuantities);

        // Assert
        // 1. Verify Inventory Update
        assertThat(item.getReservedQuantity()).isEqualTo(2);
        verify(inventoryItemRepository).save(item);

        // 2. Verify Reservation Created
        verify(stockReservationRepository).save(any(StockReservation.class));

        // 3. Verify Outbox Event
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(eventCaptor.capture());

        OutboxEvent event = eventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo("INVENTORY_RESERVED");
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    void reserveStockForOrder_ShouldThrowExceptionAndPublishFailure_WhenInsufficientStock() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String productId = "pizza-1";
        Map<String, Integer> productQuantities = Map.of(productId, 20); // Requesting 20, have 10

        InventoryItem item = InventoryItem.builder()
                .productId(productId)
                .quantity(10)
                .reservedQuantity(0)
                .build();

        when(stockReservationRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());
        when(inventoryItemRepository.findByProductId(productId)).thenReturn(Optional.of(item));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.reserveStockForOrder(orderId, productQuantities))
                .isInstanceOf(InsufficientStockException.class);

        // Verify NO Inventory Update
        verify(inventoryItemRepository, never()).save(any(InventoryItem.class));

        // Verify Failure Event
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(eventCaptor.capture());

        OutboxEvent event = eventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo("INVENTORY_UNAVAILABLE");
    }
}
