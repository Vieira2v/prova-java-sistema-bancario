package com.bruno.sistemabancario.infrastructure.repository;

import com.bruno.sistemabancario.domain.model.BankAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends MongoRepository<BankAccount, String> {

    @Query(value = "{ '_id': ?0 }", fields = "{ 'saldo' : 1 }")
    Optional<BankAccount> findBalanceById(String id);

    @Query(value = "{ 'accountNumber': ?0 }")
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    long count();
}
