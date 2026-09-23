package com.dinaxis.ttbackend.model.dto;


import com.dinaxis.ttbackend.model.OrderStatus;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class OrderSummaryDTO {

    public int id;
    public Double totalAmount;
    public OrderStatus status;
    public Timestamp createdAt;

    public List<SubOrderDTO> subOrders;
}
