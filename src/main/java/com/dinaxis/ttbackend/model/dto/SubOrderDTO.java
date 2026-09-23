package com.dinaxis.ttbackend.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class SubOrderDTO {

    @NotBlank(message = "Customer name cannot be blank")
    private String customerName;

    @NotEmpty(message = "The suborder must contain at least one item, cannot be empty")
    @Valid
    private List<OrderItemRequestDTO> items;
}
