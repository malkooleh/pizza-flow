package com.pizzaflow.inventory.repository;

import com.pizzaflow.inventory.domain.OutboxEvent;
import com.pizzaflow.inventory.domain.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
