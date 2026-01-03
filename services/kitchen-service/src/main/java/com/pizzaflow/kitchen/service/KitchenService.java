package com.pizzaflow.kitchen.service;

import com.pizzaflow.common.event.payment.PaymentEvent;
import com.pizzaflow.kitchen.domain.KitchenOrder;
import com.pizzaflow.kitchen.domain.KitchenOrderItem;
import com.pizzaflow.kitchen.domain.KitchenStatus;
import com.pizzaflow.kitchen.dto.KitchenOrderDto;
import com.pizzaflow.kitchen.repository.KitchenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenService {

    private final KitchenRepository kitchenRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_ACTIVE_ORDERS = "kitchen:active_orders";

    @Transactional
    public void processPaymentEvent(PaymentEvent event) {
        if (!"APPROVED".equals(event.getStatus())) {
            return;
        }

        log.info("Creating Kitchen Order for Order ID: {}", event.getOrderId());
        
        List<KitchenOrderItem> items = new ArrayList<>();
        items.add(new KitchenOrderItem("product-1", 1)); // Placeholder

        KitchenOrder kitchenOrder = KitchenOrder.builder()
                .orderId(event.getOrderId())
                .status(KitchenStatus.QUEUED)
                .items(items)
                .build();

        // 1. Save to PostgreSQL (History & Source of Truth)
        kitchenRepository.save(kitchenOrder);

        // 2. Save to Redis (Active Queue for High Performance)
        KitchenOrderDto dto = mapToDto(kitchenOrder);
        redisTemplate.opsForHash().put(REDIS_KEY_ACTIVE_ORDERS, dto.getOrderId().toString(), dto);

        broadcastUpdate(kitchenOrder);
    }

    public List<KitchenOrderDto> getActiveQueue() {
        // 1. Try to fetch from Redis
        List<Object> values = redisTemplate.opsForHash().values(REDIS_KEY_ACTIVE_ORDERS);
        if (values != null && !values.isEmpty()) {
            return values.stream()
                    .map(o -> (KitchenOrderDto) o)
                    .collect(Collectors.toList());
        }
        
        // 2. Fallback to DB if Redis is empty (or on cold start)
        return kitchenRepository.findAll().stream()
                .filter(o -> o.getStatus() != KitchenStatus.COMPLETED)
                .map(this::mapToDto)
                .peek(dto -> redisTemplate.opsForHash().put(REDIS_KEY_ACTIVE_ORDERS, dto.getOrderId().toString(), dto)) // Repopulate Redis
                .collect(Collectors.toList());
    }

    @Transactional
    public KitchenOrderDto updateStatus(Long orderId, KitchenStatus newStatus) {
        KitchenOrder order = kitchenRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Kitchen Order not found: " + orderId));
        
        order.setStatus(newStatus);
        kitchenRepository.save(order);
        
        // Update Redis
        if (newStatus == KitchenStatus.COMPLETED) {
            // Remove from active queue
            redisTemplate.opsForHash().delete(REDIS_KEY_ACTIVE_ORDERS, order.getOrderId().toString());
        } else {
            // Update status in active queue
            KitchenOrderDto dto = mapToDto(order);
            redisTemplate.opsForHash().put(REDIS_KEY_ACTIVE_ORDERS, order.getOrderId().toString(), dto);
        }

        broadcastUpdate(order);
        
        return mapToDto(order);
    }

    private void broadcastUpdate(KitchenOrder order) {
        log.info("Broadcasting Kitchen Update: {}", order);
        messagingTemplate.convertAndSend("/topic/kitchen/updates", mapToDto(order));
    }

    private KitchenOrderDto mapToDto(KitchenOrder order) {
        return KitchenOrderDto.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .items(order.getItems())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
