package com.bruno.sistemabancario.repositories;

import com.bruno.sistemabancario.domain.model.BankAccount;
import com.bruno.sistemabancario.infrastructure.adapter.persistence.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class BankAccountRepositoryTests {

    @Autowired
    private BankAccountRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        BankAccount account1 = new BankAccount();
        account1.setId("123");
        account1.setBalance(BigDecimal.valueOf(1000.0));
        account1.setAccountNumber("1231413");

        BankAccount account2 = new BankAccount();
        account2.setId("456");
        account2.setBalance(BigDecimal.valueOf(500.0));
        account2.setAccountNumber("12343422");

        repository.saveAll(List.of(account1, account2));
    }

    @Test
    void testFindBalanceById() {
        Optional<BankAccount> result = repository.findById("123");

        assertThat(result).isPresent();
        assertThat(result.get().getBalance().doubleValue()).isEqualTo(1000.0);
    }

    @Test
    void testFindByAccountNumber() {
        Optional<BankAccount> result = repository.findByAccountNumber("12343422");

        assertThat(result).isPresent();
        assertThat(result.get().getAccountNumber()).isEqualTo("12343422");
    }

    @Test
    void testCount() {
        long initialCount = repository.count();

        BankAccount account = new BankAccount();
        account.setId("789");
        account.setBalance(BigDecimal.valueOf(200.0));
        account.setAccountNumber("4124435312");
        repository.save(account);

        long newCount = repository.count();

        assertThat(newCount).isEqualTo(initialCount + 1);
    }
}
