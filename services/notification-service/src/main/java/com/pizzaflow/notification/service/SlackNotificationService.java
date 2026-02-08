package com.pizzaflow.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SlackNotificationService {

    // In a real application, this would use WebClient to POST to a configured Slack Webhook URL.
    // For this MVP/Phase, we will simulate the behavior and log the output.

    public void sendNotification(String message) {
        log.info("SLACK NOTIFICATION: {}", message);
    }
}
