package com.bruno.sistemabancario.application.ports.output;

import com.bruno.sistemabancario.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TransactionRepositoryPort {

    Page<Transaction> findAllByAccountNumber(String accountNumber, Pageable pageable);
    long count();
    long countByStatus(String status);
    Optional<Transaction> findById(String id);
    Transaction save(Transaction transaction);

}
