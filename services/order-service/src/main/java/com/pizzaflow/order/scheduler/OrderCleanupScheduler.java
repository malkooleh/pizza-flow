package com.pizzaflow.order.scheduler;

import com.pizzaflow.order.domain.Order;
import com.pizzaflow.order.domain.OrderStatus;
import com.pizzaflow.order.repository.OrderRepository;
import com.pizzaflow.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void cancelUnpaidOrders() {
        // Find orders pending for more than 15 minutes
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);

        if (!expiredOrders.isEmpty()) {
            log.info("Found {} expired PENDING orders to cancel.", expiredOrders.size());
            for (Order order : expiredOrders) {
                try {
                    log.info("Auto-cancelling expired order: {}", order.getId());
                    // Reuse the existing payment failure logic which triggers cancellation
                    // Or call a dedicated cancelOrder method if we had one.
                    // For now, processPaymentFailure effectively cancels it + emits event.
                    orderService.processPaymentFailure(order.getId());
                } catch (Exception e) {
                    log.error("Failed to auto-cancel order {}", order.getId(), e);
                }
            }
        }
    }
}
