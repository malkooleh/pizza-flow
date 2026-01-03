package com.pizzaflow.kitchen.dto;

import com.pizzaflow.kitchen.domain.KitchenOrderItem;
import com.pizzaflow.kitchen.domain.KitchenStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class KitchenOrderDto {
    private Long id;
    private Long orderId;
    private KitchenStatus status;
    private List<KitchenOrderItem> items;
    private LocalDateTime createdAt;
}
