package com.pizzaflow.delivery.repository;

import com.pizzaflow.delivery.domain.DeliveryZone;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {

    @Query(value = "SELECT * FROM delivery_zone dz WHERE ST_Within(:point, dz.area) AND dz.is_active = true LIMIT 1", nativeQuery = true)
    Optional<DeliveryZone> findZoneForPoint(@Param("point") Point point);
}
