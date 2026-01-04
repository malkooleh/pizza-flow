package com.pizzaflow.inventory.repository;

import com.pizzaflow.inventory.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    
    List<StockReservation> findByOrderId(Long orderId);
    
    Optional<StockReservation> findByOrderIdAndInventoryItemId(Long orderId, Long inventoryItemId);
}
