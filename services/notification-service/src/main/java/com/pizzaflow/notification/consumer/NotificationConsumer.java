package com.pizzaflow.notification.consumer;

import com.pizzaflow.common.event.OrderCancelledEvent;
import com.pizzaflow.common.event.OrderCreatedEvent;
import com.pizzaflow.common.event.KitchenReadyEvent;
import com.pizzaflow.common.event.DeliveryAssignedEvent;
import com.pizzaflow.common.event.payment.PaymentEvent;
import com.pizzaflow.notification.service.NotificationService;
import com.pizzaflow.notification.service.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final SlackNotificationService slackNotificationService;
    
    // In a real app, customer email would be part of the User Service or Auth Token.
    // For MVP, we'll hardcode or extract from event if available. 
    // Assuming a placeholder email since OrderCreatedEvent currently has customerId but not email.
    private static final String DEFAULT_EMAIL = "customer@example.com"; 

    @KafkaListener(topics = "order.created", groupId = "notification-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event);
        notificationService.sendOrderConfirmation(DEFAULT_EMAIL, event.getOrderId());
        slackNotificationService.sendNotification("New Order Received! ID: " + event.getOrderId());
    }

    @KafkaListener(topics = "payment.completed", groupId = "notification-service-group")
    public void handlePaymentCompleted(PaymentEvent event) {
        if ("APPROVED".equals(event.getStatus())) {
            log.info("Received PaymentEvent (Approved): {}", event);
            // Assuming amount is available or fetched. For MVP, we'll just notify.
            notificationService.sendPaymentConfirmation(DEFAULT_EMAIL, event.getOrderId(), 0.00); 
            slackNotificationService.sendNotification("Payment Received for Order ID: " + event.getOrderId());
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "notification-service-group")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent: {}", event);
        notificationService.sendOrderCancellation(DEFAULT_EMAIL, event.getOrderId(), event.getReason());
        slackNotificationService.sendNotification("Order Cancelled! ID: " + event.getOrderId() + ", Reason: " + event.getReason());
    }

    @KafkaListener(topics = "kitchen.ready", groupId = "notification-service-group")
    public void handleKitchenReady(KitchenReadyEvent event) {
        log.info("Received KitchenReadyEvent: {}", event);
        notificationService.sendOrderReady(DEFAULT_EMAIL, event.getOrderId());
        slackNotificationService.sendNotification("Order Ready in Kitchen! ID: " + event.getOrderId());
    }
    
    @KafkaListener(topics = "delivery.assigned", groupId = "notification-service-group")
    public void handleDeliveryAssigned(DeliveryAssignedEvent event) {
        log.info("Received DeliveryAssignedEvent: {}", event);
        notificationService.sendDeliveryUpdate(DEFAULT_EMAIL, event.getOrderId(), "Antigravity Courier");
        slackNotificationService.sendNotification("Delivery Assigned for Order ID: " + event.getOrderId());
    }
}
