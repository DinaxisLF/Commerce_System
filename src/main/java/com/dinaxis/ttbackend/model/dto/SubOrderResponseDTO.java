package com.dinaxis.ttbackend.model.dto;

import com.dinaxis.ttbackend.model.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SubOrderResponseDTO {

    private int orderId;
    private int subOrderId;
    private String customerName;
    private BigDecimal subtotalAmount;
    private OrderStatus status;
    private List<OrderItemResponseDTO> items;
}
