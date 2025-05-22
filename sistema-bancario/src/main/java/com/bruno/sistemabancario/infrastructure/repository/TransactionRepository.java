package com.bruno.sistemabancario.infrastructure.repository;

import com.bruno.sistemabancario.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    @Query("{ $or: [ { 'sourceAccount': ?0 }, { 'destinationAccount': ?0 } ] }")
    Page<Transaction> findAllByAccountNumber(String accountNumber, Pageable pageable);
}
