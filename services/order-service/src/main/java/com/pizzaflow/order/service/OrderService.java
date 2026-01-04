package com.pizzaflow.order.service;

import com.pizzaflow.order.domain.Order;
import com.pizzaflow.order.domain.OrderItem;
import com.pizzaflow.order.domain.OrderStatus;
import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.exception.ResourceNotFoundException;
import com.pizzaflow.order.domain.OrderEvent;
import com.pizzaflow.order.producer.OrderEventPublisher;
import com.pizzaflow.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
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

        return savedOrder;
    }

    @Transactional
    public void processPaymentSuccess(Long orderId) {
        Order order = getOrder(orderId);
        sendEvent(order, OrderEvent.PAYMENT_SUCCESS);
    }

    @Transactional
    public void processPaymentFailure(Long orderId) {
        Order order = getOrder(orderId);
        sendEvent(order, OrderEvent.PAYMENT_FAILURE);
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

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
