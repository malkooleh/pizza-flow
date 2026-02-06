package com.pizzaflow.order.service;

import com.pizzaflow.order.domain.Order;
import com.pizzaflow.order.domain.OrderEvent;
import com.pizzaflow.order.domain.OrderStatus;
import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.dto.OrderItemDto;
import com.pizzaflow.order.producer.OrderEventPublisher;
import com.pizzaflow.order.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private StateMachine<OrderStatus, OrderEvent> stateMachine;

    @Mock
    private Counter counter;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // Lenient stubbing for metrics to avoid unnecessary noise in setup
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
    }

    @Test
    void createOrder_ShouldSaveOrderAndPublishEvent() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setDeliveryAddress("123 Pizza St");
        request.setLatitude(40.7128);
        request.setLongitude(-74.0060);

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId("pizza-1");
        itemDto.setQuantity(2);
        itemDto.setUnitPrice(new BigDecimal("15.00"));
        request.setItems(List.of(itemDto));

        Order savedOrder = Order.builder()
                .id(100L)
                .customerId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("30.00")) // 15 * 2
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);

        // Verify Repository interaction
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getCustomerId()).isEqualTo(1L);
        assertThat(capturedOrder.getTotalAmount()).isEqualByComparingTo("30.00");
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(capturedOrder.getItems()).hasSize(1);
        assertThat(capturedOrder.getItems().get(0).getUnitPrice()).isEqualByComparingTo("15.00");

        // Verify Event Publishing
        verify(orderEventPublisher).publishOrderCreatedEvent(savedOrder);

        // Verify Metrics
        verify(meterRegistry).counter("pizzaflow.orders.created");
        verify(counter).increment();
    }

    @Test
    void processPaymentSuccess_ShouldTransitionToPaid() {
        // Arrange
        Long orderId = 1L;
        Order order = Order.builder().id(orderId).status(OrderStatus.PENDING).build();

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        // Mock State Machine Factory and Accessor
        when(stateMachineFactory.getStateMachine(orderId.toString())).thenReturn(stateMachine);
        when(stateMachine.stopReactively()).thenReturn(reactor.core.publisher.Mono.empty());
        when(stateMachine.startReactively()).thenReturn(reactor.core.publisher.Mono.empty());

        org.springframework.statemachine.region.Region<OrderStatus, OrderEvent> region = mock(
                org.springframework.statemachine.region.Region.class);
        when(stateMachine.getStateMachineAccessor())
                .thenReturn(mock(org.springframework.statemachine.access.StateMachineAccessor.class));
        doNothing().when(stateMachine.getStateMachineAccessor()).doWithAllRegions(any()); // Mock void method

        when(stateMachine.sendEvent(any(reactor.core.publisher.Mono.class)))
                .thenReturn(reactor.core.publisher.Flux.empty());

        // Mock State after transition
        org.springframework.statemachine.state.State<OrderStatus, OrderEvent> state = mock(
                org.springframework.statemachine.state.State.class);
        when(state.getId()).thenReturn(OrderStatus.PAID);
        when(stateMachine.getState()).thenReturn(state);

        // Act
        orderService.processPaymentSuccess(orderId);

        // Assert
        verify(stateMachine).sendEvent(any(reactor.core.publisher.Mono.class));
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.PAID));
    }
}
