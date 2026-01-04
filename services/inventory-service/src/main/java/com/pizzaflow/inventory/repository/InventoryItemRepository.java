package com.pizzaflow.inventory.repository;

import com.pizzaflow.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    
    Optional<InventoryItem> findByProductId(String productId);

    @Lock(LockModeType.OPTIMISTIC)
    Optional<InventoryItem> findWithLockById(Long id);
}
