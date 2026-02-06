package com.pizzaflow.order.service.integration;

import com.pizzaflow.order.domain.Order;
import com.pizzaflow.order.domain.OrderStatus;
import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.dto.OrderItemDto;
import com.pizzaflow.order.repository.OrderRepository;
import com.pizzaflow.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldCreateOrderAndPersistToDatabase() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setDeliveryAddress("Integration Test St");
        request.setLatitude(0.0);
        request.setLongitude(0.0);

        OrderItemDto item = new OrderItemDto();
        item.setProductId("pizza-test");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("10.00"));
        request.setItems(List.of(item));

        // Act
        Order createdOrder = orderService.createOrder(request);

        // Assert
        assertThat(createdOrder.getId()).isNotNull();

        // Verify Persistence
        Order retrievedOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
        assertThat(retrievedOrder.getCustomerId()).isEqualTo(1L);
        assertThat(retrievedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(retrievedOrder.getItems()).hasSize(1);
    }
}
