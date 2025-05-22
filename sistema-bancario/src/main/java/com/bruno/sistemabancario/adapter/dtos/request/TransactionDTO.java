package com.bruno.sistemabancario.adapter.dtos.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDTO {

    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal value;
}
