package com.bruno.sistemabancario.repositories;

import com.bruno.sistemabancario.domain.model.Transaction;
import com.bruno.sistemabancario.infrastructure.adapter.persistence.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@DataMongoTest
public class TransactionRepositoryTests {

    @Autowired
    private TransactionRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        Transaction t1 = new Transaction();
        t1.setId("1");
        t1.setSourceAccount("3123124");
        t1.setDestinationAccount("3123123");
        t1.setStatus("COMPLETED");

        Transaction t2 = new Transaction();
        t2.setId("2");
        t2.setSourceAccount("3123143");
        t2.setDestinationAccount("3123124");
        t2.setStatus("PENDING");

        Transaction t3 = new Transaction();
        t3.setId("3");
        t3.setSourceAccount("543232");
        t3.setDestinationAccount("4523424");
        t3.setStatus("COMPLETED");

        repository.saveAll(List.of(t1, t2, t3));
    }

    @Test
    void testFindAllByAccountNumber() {
        Page<Transaction> page = repository.findAllByAccountNumber("3123124", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting("id")
                .containsExactlyInAnyOrder("1", "2");
    }

    @Test
    void testCount() {
        long total = repository.count();
        assertThat(total).isEqualTo(3);
    }

    @Test
    void testCountByStatus() {
        long completedCount = repository.countByStatus("COMPLETED");
        long pendingCount = repository.countByStatus("PENDING");
        long canceledCount = repository.countByStatus("CANCELED");

        assertThat(completedCount).isEqualTo(2);
        assertThat(pendingCount).isEqualTo(1);
        assertThat(canceledCount).isEqualTo(0);
    }
}
