package com.pizzaflow.order.service;

import com.pizzaflow.order.domain.Order;
import com.pizzaflow.order.domain.OrderEvent;
import com.pizzaflow.order.domain.OrderItem;
import com.pizzaflow.order.domain.OrderStatus;
import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.dto.OrderItemDto;
import com.pizzaflow.order.dto.OrderResponse;
import com.pizzaflow.order.exception.ResourceNotFoundException;
import com.pizzaflow.order.producer.OrderEventPublisher;
import com.pizzaflow.order.repository.OrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory;
    private final MeterRegistry meterRegistry;

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, OrderStatus status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        // In a real app, we might want to publish an event here too
        // orderEventPublisher.publishOrderStatusChangedEvent(savedOrder);

        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        meterRegistry.counter("pizzaflow.orders.created").increment();

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .deliveryAddress(request.getDeliveryAddress())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(itemDto -> OrderItem.builder()
                        .order(order)
                        .productId(itemDto.getProductId())
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .build())
                .toList();

        order.setItems(items);
        order.setTotalAmount(calculateTotal(items));

        Order savedOrder = orderRepository.save(order);

        // Publish Event
        orderEventPublisher.publishOrderCreatedEvent(savedOrder);

        return mapToResponse(savedOrder);
    }

    @Transactional
    public void processPaymentSuccess(UUID orderId) {
        Order order = getOrder(orderId);
        sendEvent(order, OrderEvent.PAYMENT_SUCCESS);
    }

    @Transactional
    public void processPaymentFailure(UUID orderId) {
        Order order = getOrder(orderId);
        sendEvent(order, OrderEvent.PAYMENT_FAILURE);

        // Compensating Transaction: Alert other services
        orderEventPublisher.publishOrderCancelledEvent(orderId, "Payment Failed");
    }

    private void sendEvent(Order order, OrderEvent event) {
        StateMachine<OrderStatus, OrderEvent> sm = build(order);

        // Use reactive API instead of deprecated synchronous method
        sm.sendEvent(Mono.just(MessageBuilder.withPayload(event)
                .setHeader("orderId", order.getId())
                .build()))
                .blockLast(); // Block for transactional consistency

        // For MVP: Manually sync state back to Entity (Simpler than Interceptors for
        // now)
        OrderStatus newState = sm.getState().getId();
        if (newState != order.getStatus()) {
            order.setStatus(newState);
            orderRepository.save(order);
        }
    }

    private StateMachine<OrderStatus, OrderEvent> build(Order order) {
        StateMachine<OrderStatus, OrderEvent> sm = stateMachineFactory.getStateMachine(order.getId().toString());
        sm.stopReactively().block();
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> sma.resetStateMachineReactively(
                        new DefaultStateMachineContext<>(order.getStatus(), null, null, null))
                        .block());
        sm.startReactively().block();
        return sm;
    }

    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public OrderResponse getOrderResponse(UUID id) {
        return mapToResponse(getOrder(id));
    }

    public List<OrderResponse> getOrdersResponseByCustomer(UUID customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(item -> {
                            OrderItemDto dto = new OrderItemDto();
                            dto.setProductId(item.getProductId());
                            dto.setQuantity(item.getQuantity());
                            dto.setUnitPrice(item.getUnitPrice());
                            return dto;
                        })
                        .toList())
                .deliveryAddress(order.getDeliveryAddress())
                .longitude(order.getLongitude())
                .latitude(order.getLatitude())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public List<Order> getOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
