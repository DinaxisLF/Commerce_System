package com.dinaxis.ttbackend.model.dto;

import com.dinaxis.ttbackend.model.PaymentMethod;
import com.dinaxis.ttbackend.model.TransactionCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutcomeRequestDTO {

    @NotNull(message = "Category cannot be null")
    private TransactionCategory category;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    // Defaults to CASH when omitted.
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}
