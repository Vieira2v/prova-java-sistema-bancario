package com.bruno.sistemabancario.adapter.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDTO {

    @NotBlank(message = "Source account is required")
    private String sourceAccount;

    @NotBlank(message = "Destination account is required")
    private String destinationAccount;

    @NotNull(message = "Transaction value is required")
    @Positive(message = "Transaction value must be greater than 0")
    private BigDecimal value;
}
