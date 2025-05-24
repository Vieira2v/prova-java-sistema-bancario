package com.bruno.sistemabancario.application.ports.output;

import com.bruno.sistemabancario.domain.model.BankAccount;

import java.util.Optional;

public interface BankAccountRepositoryPort {

    Optional<BankAccount> findByAccountNumber(String accountNumber);
    BankAccount save(BankAccount bankAccount);
    long count();
    Optional<BankAccount> findById(String id);
}
