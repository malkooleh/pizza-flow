package com.pizzaflow.kitchen.service;

import com.pizzaflow.common.event.payment.PaymentEvent;
import com.pizzaflow.kitchen.domain.KitchenOrder;
import com.pizzaflow.kitchen.domain.KitchenStatus;
import com.pizzaflow.kitchen.dto.KitchenOrderDto;
import com.pizzaflow.kitchen.repository.KitchenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private KitchenRepository kitchenRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KitchenService kitchenService;

    @Test
    void processPaymentEvent_ShouldCreateOrderAndAddToQueue() {
        // Arrange
        PaymentEvent event = new PaymentEvent();
        event.setOrderId(101L);
        event.setStatus("APPROVED");

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(kitchenRepository.save(any(KitchenOrder.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        kitchenService.processPaymentEvent(event);

        // Assert
        ArgumentCaptor<KitchenOrder> orderCaptor = ArgumentCaptor.forClass(KitchenOrder.class);
        verify(kitchenRepository).save(orderCaptor.capture());

        KitchenOrder savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getOrderId()).isEqualTo(101L);
        assertThat(savedOrder.getStatus()).isEqualTo(KitchenStatus.QUEUED);

        // Verify Redis
        verify(hashOperations).put(eq("kitchen:active_orders"), eq("101"), any(KitchenOrderDto.class));

        // Verify WebSocket Broadcast
        verify(messagingTemplate).convertAndSend(eq("/topic/kitchen/updates"), any(KitchenOrderDto.class));
    }

    @Test
    void updateStatus_ShouldUpdateStatusAndPublishKafkaEvent_WhenReady() {
        // Arrange
        Long orderId = 101L;
        KitchenOrder existingOrder = KitchenOrder.builder()
                .id(1L)
                .orderId(orderId)
                .status(KitchenStatus.PREPARING)
                .build();

        when(kitchenRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        // Act
        KitchenOrderDto result = kitchenService.updateStatus(orderId, KitchenStatus.READY);

        // Assert
        assertThat(result.getStatus()).isEqualTo(KitchenStatus.READY);

        // Verify DB Save
        verify(kitchenRepository).save(any(KitchenOrder.class));

        // Verify Redis Update
        verify(hashOperations).put(eq("kitchen:active_orders"), eq("101"), any(KitchenOrderDto.class));

        // Verify Kafka Event
        verify(kafkaTemplate).send(eq("kitchen.ready"), eq("101"), any());
    }
}
