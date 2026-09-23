package com.dinaxis.ttbackend.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;


@Data
public class OrderRequestDTO {

    @NotEmpty(message = "The order must contain at least one suborder, cannot be empty")
    @Valid
    private List<SubOrderDTO> subOrders;

}
