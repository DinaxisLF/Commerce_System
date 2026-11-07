package com.dinaxis.ttbackend.model.dto;


import com.dinaxis.ttbackend.model.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

// Full order detail: order header plus every sub-order and its items.
@Data
public class OrderSummaryDTO {

    private int orderId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Timestamp createdAt;
    private List<SubOrderResponseDTO> subOrders;
}
