package com.pizzaflow.order.domain;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAID,
    PREPARING,
    READY_FOR_DELIVERY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
