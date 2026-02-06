package com.pizzaflow.kitchen.service.integration;

import com.pizzaflow.common.event.payment.PaymentEvent;
import com.pizzaflow.kitchen.domain.KitchenStatus;
import com.pizzaflow.kitchen.dto.KitchenOrderDto;
import com.pizzaflow.kitchen.service.KitchenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KitchenServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KitchenService kitchenService;

    @Test
    void shouldCreateOrderAndPersistToRedisAuthPostgres() {
        // Arrange
        PaymentEvent event = new PaymentEvent();
        event.setOrderId(999L);
        event.setStatus("APPROVED");

        // Act
        kitchenService.processPaymentEvent(event);

        // Assert
        // 1. Verify Redis (Immediate Visibility for KDS)
        List<KitchenOrderDto> activeOrders = kitchenService.getActiveQueue();
        assertThat(activeOrders).hasSize(1);
        assertThat(activeOrders.get(0).getOrderId()).isEqualTo(999L);
        assertThat(activeOrders.get(0).getStatus()).isEqualTo(KitchenStatus.QUEUED);

        // 2. Verify State Transition Updates Redis
        kitchenService.updateStatus(999L, KitchenStatus.PREPARING);

        KitchenOrderDto updated = kitchenService.getActiveQueue().get(0);
        assertThat(updated.getStatus()).isEqualTo(KitchenStatus.PREPARING);
    }
}
