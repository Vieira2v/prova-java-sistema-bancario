package com.bruno.sistemabancario.adapter.dtos.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionsUserDTO {

    private String id;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal value;
    private LocalDate transactionDate ;
    private String status;
}
