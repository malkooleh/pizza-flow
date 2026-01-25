package com.pizzaflow.delivery.dto;

import com.pizzaflow.delivery.domain.CourierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierResponse {
    private Long id;
    private String name;
    private CourierStatus status;
    private Double longitude;
    private Double latitude;
}
