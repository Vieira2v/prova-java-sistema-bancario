package com.bruno.sistemabancario.infrastructure.adapter.out.persistence;

import com.bruno.sistemabancario.domain.model.BankAccount;
import com.bruno.sistemabancario.application.ports.output.BankAccountRepositoryPort;
import com.bruno.sistemabancario.infrastructure.adapter.persistence.BankAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BankAccountRepositoryAdapter implements BankAccountRepositoryPort {

    private final BankAccountRepository repository;

    public BankAccountRepositoryAdapter(BankAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BankAccount> findBalanceById(String id) {
        return repository.findBalanceById(id);
    }

    @Override
    public Optional<BankAccount> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber);
    }

    @Override
    public BankAccount save(BankAccount bankAccount) {
        return repository.save(bankAccount);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public Optional<BankAccount> findById(String id) {
        return repository.findById(id);
    }


}
