package com.dinaxis.ttbackend.model.dto;

import com.dinaxis.ttbackend.model.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO{
    private Integer orderId;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
}