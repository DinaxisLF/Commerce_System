package com.dinaxis.ttbackend.model.dto;

import com.dinaxis.ttbackend.model.OrderStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class SubOrderResponseDTO {

    private int OrderId;
    private int subOrderId;
    private String customerName;
    private List<OrderItemRequestDTO> items;
    private OrderStatus subOrderStatus;
}
