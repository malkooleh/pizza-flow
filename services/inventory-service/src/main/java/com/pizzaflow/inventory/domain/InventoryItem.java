package com.pizzaflow.inventory.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "inventory_item")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true, length = 100)
    private String productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @Version
    @Column(name = "version")
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryItem that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public boolean canReserve(Integer requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }

    public void reserve(Integer requestedQuantity) {
        if (!canReserve(requestedQuantity)) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
        this.reservedQuantity += requestedQuantity;
    }

    public void release(Integer quantityToRelease) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantityToRelease);
    }

    public void commit(Integer quantityToCommit) {
        this.quantity -= quantityToCommit;
        this.reservedQuantity -= quantityToCommit;
    }
}
