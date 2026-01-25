package com.pizzaflow.delivery.repository;

import com.pizzaflow.delivery.domain.Courier;
import com.pizzaflow.delivery.domain.CourierStatus;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {

    List<Courier> findByStatus(CourierStatus status);

    @Query(value = "SELECT * FROM courier c WHERE c.status = 'AVAILABLE' " +
            "ORDER BY c.current_location <-> :location LIMIT 1", nativeQuery = true)
    Courier findNearestAvailableCourier(@Param("location") Point location);
}
