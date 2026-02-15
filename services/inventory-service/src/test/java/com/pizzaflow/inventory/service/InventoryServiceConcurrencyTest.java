package com.pizzaflow.inventory.service;

import com.pizzaflow.inventory.domain.InventoryItem;
import com.pizzaflow.inventory.repository.InventoryItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
@ActiveProfiles("test")
public class InventoryServiceConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void testConcurrentStockReservation() throws InterruptedException {
        // Arrange
        String productId = "concurrent-pizza-" + UUID.randomUUID();
        InventoryItem item = InventoryItem.builder()
                .productId(productId)
                .productName("Concurrent Pizza")
                .quantity(10)
                .reservedQuantity(0)
                .unit("PIECE")
                .build();
        inventoryItemRepository.saveAndFlush(item);

        int numberOfThreads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    transactionTemplate.execute(status -> {
                        inventoryService.reserveStockForOrder(UUID.randomUUID(), Map.of(productId, 1));
                        return null;
                    });
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Assert
        InventoryItem updatedItem = inventoryItemRepository.findByProductId(productId).orElseThrow();

        // With 10 available, and 20 threads requesting 1 each:
        // Success should be exactly 10, failure should be 10.
        // Reserved quantity should be exactly 10.

        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failureCount.get()).isEqualTo(10);
        assertThat(updatedItem.getReservedQuantity()).isEqualTo(10);
        assertThat(updatedItem.getAvailableQuantity()).isEqualTo(0);
    }
}
