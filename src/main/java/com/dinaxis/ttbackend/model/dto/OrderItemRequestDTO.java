package com.dinaxis.ttbackend.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class OrderItemRequestDTO {
    @NotNull(message = "The product list cannot be null")
    private Integer productId;

    @NotNull(message = "The quantity cannot be null")
    @Min(value = 1, message = "The quantity must be at least 1")
    private Integer quantity;
}
