package com.pizzaflow.order.config;

import com.pizzaflow.order.domain.OrderEvent;
import com.pizzaflow.order.domain.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
@Slf4j
public class OrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states) throws Exception {
        states
                .withStates()
                .initial(OrderStatus.PENDING)
                .states(EnumSet.allOf(OrderStatus.class))
                .end(OrderStatus.COMPLETED)
                .end(OrderStatus.CANCELLED)
                .end(OrderStatus.DELIVERED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions) throws Exception {
        transitions
                // Happy Path
                .withExternal().source(OrderStatus.PENDING).target(OrderStatus.PAID).event(OrderEvent.PAYMENT_SUCCESS)
                .and()
                .withExternal().source(OrderStatus.PAID).target(OrderStatus.PREPARING)
                .event(OrderEvent.KITCHEN_ACCEPTED)
                .and()
                .withExternal().source(OrderStatus.PREPARING).target(OrderStatus.READY_FOR_DELIVERY)
                .event(OrderEvent.KITCHEN_READY)
                .and()
                .withExternal().source(OrderStatus.READY_FOR_DELIVERY).target(OrderStatus.OUT_FOR_DELIVERY)
                .event(OrderEvent.COURIER_ASSIGNED)
                .and()
                .withExternal().source(OrderStatus.OUT_FOR_DELIVERY).target(OrderStatus.DELIVERED)
                .event(OrderEvent.DELIVERY_COMPLETED)

                // Failures / Cancellation
                .and()
                .withExternal().source(OrderStatus.PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEvent.PAYMENT_FAILURE)
                .and()
                .withExternal().source(OrderStatus.PENDING).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL)
                .and()
                .withExternal().source(OrderStatus.PAID).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL);
    }
}
