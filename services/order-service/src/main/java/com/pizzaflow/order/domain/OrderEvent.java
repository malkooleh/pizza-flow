package com.pizzaflow.order.domain;

public enum OrderEvent {
    PAYMENT_SUCCESS,
    PAYMENT_FAILURE,
    KITCHEN_ACCEPTED,
    KITCHEN_READY,
    COURIER_ASSIGNED,
    DELIVERY_COMPLETED,
    CANCEL
}
