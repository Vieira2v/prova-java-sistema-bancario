package com.bruno.sistemabancario.infrastructure.adapter.out.persistence;

import com.bruno.sistemabancario.domain.model.Transaction;
import com.bruno.sistemabancario.application.ports.output.TransactionRepositoryPort;
import com.bruno.sistemabancario.infrastructure.adapter.persistence.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionRepository repository;

    public TransactionRepositoryAdapter(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Transaction> findAllByAccountNumber(String accountNumber, Pageable pageable) {
        return repository.findAllByAccountNumber(accountNumber, pageable);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Transaction save(Transaction transaction) {
        return repository.save(transaction);
    }
}
