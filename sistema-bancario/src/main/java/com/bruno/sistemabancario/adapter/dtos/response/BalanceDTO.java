package com.bruno.sistemabancario.adapter.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceDTO {

    private BigDecimal balance;
}
