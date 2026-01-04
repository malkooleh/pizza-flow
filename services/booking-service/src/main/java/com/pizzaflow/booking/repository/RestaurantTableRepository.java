package com.pizzaflow.booking.repository;

import com.pizzaflow.booking.domain.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByTableNumber(Integer tableNumber);
}
