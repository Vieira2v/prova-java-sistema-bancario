package com.bruno.sistemabancario.adapter.dtos.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportDTO {

    private int totalAccounts;
    private int totalTransactions;
    private BigDecimal totalAmountMoved;
}
