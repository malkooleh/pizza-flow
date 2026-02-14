package com.pizzaflow.kitchen.dto;

import com.pizzaflow.kitchen.domain.KitchenOrderItem;
import com.pizzaflow.kitchen.domain.KitchenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderDto {
    private Long id;
    private UUID orderId;
    private KitchenStatus status;
    private List<KitchenOrderItem> items;
    private LocalDateTime createdAt;
}
