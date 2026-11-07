package com.dinaxis.ttbackend.model.dto;

import com.dinaxis.ttbackend.model.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO{
    private Integer orderId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
}