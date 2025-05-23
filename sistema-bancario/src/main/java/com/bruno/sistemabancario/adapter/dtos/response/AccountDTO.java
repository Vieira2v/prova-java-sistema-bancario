package com.bruno.sistemabancario.adapter.dtos.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AccountDTO {

    private String id;
    private String name;
    private String cpf;
    private BigDecimal balance;
    private LocalDate openingDate;
    private String accountNumber;
}
