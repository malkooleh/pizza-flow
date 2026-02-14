package com.pizzaflow.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;

    private static final String FROM_EMAIL = "noreply@pizzaflow.com";

    @Override
    public void sendOrderConfirmation(String email, UUID orderId) {
        log.info("Sending Order Confirmation Email to: {} for Order ID: {}", email, orderId);
        sendEmail(email, "Order Confirmation #" + orderId,
                "Your order #" + orderId + " has been received and is being processed.");
    }

    @Override
    public void sendPaymentConfirmation(String email, UUID orderId, double amount) {
        log.info("Sending Payment Confirmation Email to: {} for Order ID: {}, Amount: {}", email, orderId, amount);
        sendEmail(email, "Payment Received for Order #" + orderId,
                "We have received a payment of $" + amount + " for your order #" + orderId + ".");
    }

    @Override
    public void sendOrderCancellation(String email, UUID orderId, String reason) {
        log.info("Sending Order Cancellation Email to: {} for Order ID: {}, Reason: {}", email, orderId, reason);
        sendEmail(email, "Order Cancelled #" + orderId,
                "Your order #" + orderId + " has been cancelled. Reason: " + reason);
    }

    @Override
    public void sendOrderReady(String email, UUID orderId) {
        log.info("Sending Order Ready Email to: {} for Order ID: {}", email, orderId);
        sendEmail(email, "Order Ready for Pickup/Delivery #" + orderId,
                "Great news! Your order #" + orderId + " is hot and ready.");
    }

    @Override
    public void sendDeliveryUpdate(String email, UUID orderId, String courierName) {
        log.info("Sending Delivery Update to {}", email);
        sendEmail(email, "Order Out for Delivery #" + orderId,
                "Your order #" + orderId + " is on the way! Courier: " + courierName);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
