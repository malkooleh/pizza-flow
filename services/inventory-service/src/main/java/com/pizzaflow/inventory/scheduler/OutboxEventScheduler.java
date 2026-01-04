package com.pizzaflow.inventory.scheduler;

import com.pizzaflow.inventory.domain.OutboxEvent;
import com.pizzaflow.inventory.domain.OutboxStatus;
import com.pizzaflow.inventory.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Scheduled service that implements the Outbox Pattern for reliable event
 * publishing.
 * Polls pending/failed events from the outbox table and publishes them to
 * Kafka.
 * Runs every 5 seconds to ensure timely event delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Publishes pending and failed (retriable) events to Kafka topics.
     * Failed events are retried up to MAX_RETRY_ATTEMPTS times.
     */
    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (!pendingEvents.isEmpty()) {
            log.info("Publishing {} pending outbox events", pendingEvents.size());
            publishEvents(pendingEvents);
        }

        // Retry failed events (with exponential backoff logic could be added here)
        retryFailedEvents();
    }

    /**
     * Implements retry logic for events that previously failed to publish.
     * Uses exponential backoff: 5s, 30s, 120s retry intervals.
     * Only retries events that haven't exceeded MAX_RETRY_ATTEMPTS.
     */
    private void retryFailedEvents() {
        List<OutboxEvent> failedEvents = outboxEventRepository
                .findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.FAILED);

        if (failedEvents.isEmpty()) {
            return;
        }

        log.info("Evaluating {} failed outbox events for retry", failedEvents.size());
        int retriedCount = 0;

        for (OutboxEvent event : failedEvents) {
            // Check if max retry attempts exceeded
            if (event.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
                log.warn("Event {} has exceeded max retry attempts ({}), skipping",
                        event.getId(), MAX_RETRY_ATTEMPTS);
                continue;
            }

            // Exponential backoff: wait before retrying
            if (!shouldRetryNow(event)) {
                continue; // Skip this event, not enough time has passed
            }

            try {
                String topic = mapEventTypeToTopic(event.getEventType());
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);

                log.info("Successfully retried (attempt {}/{}) and published event {} to topic {}",
                        event.getRetryCount() + 1, MAX_RETRY_ATTEMPTS, event.getId(), topic);
                retriedCount++;

            } catch (Exception e) {
                // Increment retry count, update last attempt, keep status as FAILED
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastAttemptAt(Instant.now());
                outboxEventRepository.save(event);

                log.error("Retry attempt {}/{} failed for event {}: {}",
                        event.getRetryCount(), MAX_RETRY_ATTEMPTS, event.getId(), e.getMessage());
            }
        }

        if (retriedCount > 0) {
            log.info("Retried {} events with exponential backoff", retriedCount);
        }
    }

    /**
     * Determines if enough time has passed for the next retry attempt using
     * exponential backoff.
     * Retry intervals: 5s (1st), 30s (2nd), 120s (3rd)
     *
     * @param event The outbox event to check
     * @return true if the event should be retried now, false otherwise
     */
    private boolean shouldRetryNow(OutboxEvent event) {
        if (event.getLastAttemptAt() == null) {
            return true; // First retry, attempt immediately
        }

        long secondsSinceLastAttempt = java.time.Duration.between(event.getLastAttemptAt(), Instant.now()).getSeconds();

        // Exponential backoff: 5s, 30s, 120s
        long backoffSeconds = calculateBackoffSeconds(event.getRetryCount());

        return secondsSinceLastAttempt >= backoffSeconds;
    }

    /**
     * Calculate exponential backoff delay in seconds.
     * Formula: base_delay * (2 ^ retry_count)
     * Results: 5s, 30s, 120s for attempts 0, 1, 2
     *
     * @param retryCount Current retry count
     * @return Backoff delay in seconds
     */
    private long calculateBackoffSeconds(int retryCount) {
        return switch (retryCount) {
            case 0 -> 5; // 1st retry: 5 seconds
            case 1 -> 30; // 2nd retry: 30 seconds
            case 2 -> 120; // 3rd retry: 120 seconds (2 minutes)
            default -> 300; // Fallback: 5 minutes
        };
    }

    private void publishEvents(List<OutboxEvent> events) {
        for (OutboxEvent event : events) {
            try {
                String topic = mapEventTypeToTopic(event.getEventType());

                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);

                log.info("Published event {} to topic {}", event.getId(), topic);

            } catch (Exception e) {
                log.error("Failed to publish event {}: {}", event.getId(), e.getMessage());
                event.setStatus(OutboxStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }

    private String mapEventTypeToTopic(String eventType) {
        return switch (eventType) {
            case "INVENTORY_RESERVED" -> "inventory.reserved";
            case "INVENTORY_UNAVAILABLE" -> "inventory.unavailable";
            default -> "inventory.events";
        };
    }
}
