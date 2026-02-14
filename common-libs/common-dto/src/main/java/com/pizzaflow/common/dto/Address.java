package com.pizzaflow.common.dto;

import java.io.Serializable;

public record Address(
        String street,
        String city,
        String state,
        String zipCode,
        String country) implements Serializable {
}
