package com.pizzaflow.order.service.integration;

import com.pizzaflow.common.dto.Address;
import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.dto.OrderItemDto;
import com.pizzaflow.order.producer.OrderEventPublisher;
import com.pizzaflow.order.repository.OrderRepository;
import com.pizzaflow.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false"
})
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1)
class OrderServiceChaosTest {

        @Autowired
        private OrderService orderService;

        @Autowired
        private OrderRepository orderRepository;

        @SpyBean
        private OrderEventPublisher orderEventPublisher;

        @Test
        void shouldRollbackTransactionIfProcessFailsDuringEventPublication() {
                // Arrange
                orderRepository.deleteAll(); // Clean up for test

                CreateOrderRequest request = new CreateOrderRequest();
                request.setCustomerId(UUID.randomUUID());
                request.setDeliveryAddress(new Address("Chaos St", "Fail City", "FC", "00000", "Chaos"));
                request.setLatitude(0.0);
                request.setLongitude(0.0);

                OrderItemDto item = new OrderItemDto();
                item.setProductId("chaos-pizza");
                item.setQuantity(1);
                item.setUnitPrice(new BigDecimal("15.00"));
                request.setItems(List.of(item));

                // Simulate failure during event publication
                doThrow(new RuntimeException("Simulated Chaos Failure"))
                                .when(orderEventPublisher).publishOrderCreatedEvent(any());

                // Act & Assert
                assertThatThrownBy(() -> orderService.createOrder(request))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessage("Simulated Chaos Failure");

                // Verify: The transaction MUST have rolled back
                long count = orderRepository.count();
                assertThat(count).isEqualTo(0);
        }
}
