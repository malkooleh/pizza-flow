package com.pizzaflow.kitchen.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderItem implements Serializable {
    private String productId;
    private int quantity;
}
