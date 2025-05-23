package com.bruno.sistemabancario.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "account")
public class BankAccount {

    @Id
    private String id;

    private String name;
    private String cpf;
    private BigDecimal balance;
    private LocalDate openingDate;
    private String accountNumber;
}
