package com.dinaxis.ttbackend.model.dto;

import com.dinaxis.ttbackend.model.PaymentMethod;
import com.dinaxis.ttbackend.model.TransactionCategory;
import com.dinaxis.ttbackend.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO {

    private Integer id;
    private TransactionType type;
    private TransactionCategory category;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String description;
    private Timestamp createdAt;
    // Set for sales, null for outcomes.
    private Integer subOrderId;
}
