package com.pizzaflow.delivery.domain;

import com.pizzaflow.common.dto.Address;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delivery")
@EntityListeners(AuditingEntityListener.class)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    @ToString.Exclude
    private Courier courier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DeliveryStatus status;

    @Column(name = "delivery_location", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point deliveryLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "street")),
            @AttributeOverride(name = "city", column = @Column(name = "city")),
            @AttributeOverride(name = "state", column = @Column(name = "state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "zip_code")),
            @AttributeOverride(name = "country", column = @Column(name = "country"))
    })
    private Address deliveryAddress;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Delivery delivery))
            return false;
        return Objects.equals(id, delivery.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
