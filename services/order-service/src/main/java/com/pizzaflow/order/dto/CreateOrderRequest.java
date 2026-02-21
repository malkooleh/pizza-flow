package com.pizzaflow.order.dto;

import com.pizzaflow.common.dto.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemDto> items;

    @Valid
    private Address deliveryAddress;

    private Double longitude;
    private Double latitude;
}
