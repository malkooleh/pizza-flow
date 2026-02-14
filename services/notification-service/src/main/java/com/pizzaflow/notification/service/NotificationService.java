package com.pizzaflow.notification.service;

import java.util.UUID;

public interface NotificationService {
    void sendOrderConfirmation(String email, UUID orderId);

    void sendPaymentConfirmation(String email, UUID orderId, double amount);

    void sendOrderCancellation(String email, UUID orderId, String reason);

    void sendOrderReady(String email, UUID orderId);

    void sendDeliveryUpdate(String email, UUID orderId, String courierName);
}
