package com.dinaxis.ttbackend.model.dto;

import com.dinaxis.ttbackend.model.OrderStatus;
import com.dinaxis.ttbackend.model.PaymentMethod;
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
    private PaymentMethod paymentMethod;
    private BigDecimal amountReceived;
    private BigDecimal change;
    private List<OrderItemResponseDTO> items;
}
