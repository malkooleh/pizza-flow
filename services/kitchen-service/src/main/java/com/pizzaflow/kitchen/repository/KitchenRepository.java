package com.pizzaflow.kitchen.repository;

import com.pizzaflow.kitchen.domain.KitchenOrder;
import com.pizzaflow.kitchen.domain.KitchenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KitchenRepository extends JpaRepository<KitchenOrder, Long> {
    Optional<KitchenOrder> findByOrderId(UUID orderId);

    List<KitchenOrder> findByStatus(KitchenStatus status);
}
