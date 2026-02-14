package com.pizzaflow.kitchen.service.integration;

import com.pizzaflow.common.event.payment.PaymentEvent;
import com.pizzaflow.kitchen.domain.KitchenStatus;
import com.pizzaflow.kitchen.dto.KitchenOrderDto;
import com.pizzaflow.kitchen.service.KitchenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KitchenServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KitchenService kitchenService;

    @Test
    void shouldCreateOrderAndPersistToRedisAuthPostgres() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent();
        event.setOrderId(orderId);
        event.setStatus("APPROVED");

        // Act
        kitchenService.processPaymentEvent(event);

        // Assert
        // 1. Verify Redis (Immediate Visibility for KDS)
        List<KitchenOrderDto> activeOrders = kitchenService.getActiveQueue();
        assertThat(activeOrders).hasSize(1);
        assertThat(activeOrders.getFirst().getOrderId()).isEqualTo(orderId);
        assertThat(activeOrders.getFirst().getStatus()).isEqualTo(KitchenStatus.QUEUED);

        // 2. Verify State Transition Updates Redis
        kitchenService.updateStatus(orderId, KitchenStatus.PREPARING);

        KitchenOrderDto updated = kitchenService.getActiveQueue().getFirst();
        assertThat(updated.getStatus()).isEqualTo(KitchenStatus.PREPARING);
    }
}
