package com.pizzaflow.notification.service;

public interface NotificationService {
    void sendOrderConfirmation(String email, Long orderId);
    void sendPaymentConfirmation(String email, Long orderId, double amount);
    void sendOrderCancellation(String email, Long orderId, String reason);
    void sendOrderReady(String email, Long orderId);
    void sendDeliveryUpdate(String email, Long orderId, String courierName);
}
