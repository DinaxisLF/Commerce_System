package com.dinaxis.ttbackend.model.dto;

import com.dinaxis.ttbackend.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubOrderStatusRequestDTO {

    @NotBlank(message = "Status cannot be blank")
    private String status;

    // Required when status is COMPLETED.
    private PaymentMethod paymentMethod;

    // Required for CASH payments, must cover the subtotal.
    @DecimalMin(value = "0.01", message = "Amount received must be greater than zero")
    private BigDecimal amountReceived;
}
