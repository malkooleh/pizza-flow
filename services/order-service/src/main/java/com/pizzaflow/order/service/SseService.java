package com.pizzaflow.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(UUID orderId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 minutes timeout

        emitter.onCompletion(() -> {
            log.info("SSE completion for order: {}", orderId);
            emitters.remove(orderId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE timeout for order: {}", orderId);
            emitters.remove(orderId);
            emitter.complete();
        });

        emitter.onError((e) -> {
            log.error("SSE error for order: {}", orderId, e);
            emitters.remove(orderId);
        });

        emitters.put(orderId, emitter);
        
        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data(Map.of("message", "Tracking initialized", "orderId", orderId)));
        } catch (IOException e) {
            log.error("Failed to send INIT event for order: {}", orderId);
            emitters.remove(orderId);
        }

        return emitter;
    }

    public void broadcastUpdate(UUID orderId, Object data) {
        SseEmitter emitter = emitters.get(orderId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ORDER_UPDATE")
                        .data(data));
                log.info("Broadcasted SSE update for order: {}", orderId);
            } catch (IOException e) {
                log.error("Failed to send SSE update for order: {}, removing emitter", orderId);
                emitters.remove(orderId);
            }
        }
    }
}
