package com.pizzaflow.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponse {
    private Long id;
    private String productId;
    private String productName;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String unit;
}
