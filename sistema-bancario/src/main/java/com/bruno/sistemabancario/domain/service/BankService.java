package com.bruno.sistemabancario.domain.service;

import com.bruno.sistemabancario.adapter.dtos.request.AccountOpeningDTO;
import com.bruno.sistemabancario.adapter.dtos.request.TransactionDTO;
import com.bruno.sistemabancario.adapter.dtos.response.AccountDTO;
import com.bruno.sistemabancario.adapter.dtos.response.BalanceDTO;
import com.bruno.sistemabancario.adapter.dtos.response.ReportDTO;
import com.bruno.sistemabancario.adapter.dtos.response.TransactionsUserDTO;
import com.bruno.sistemabancario.domain.exceptions.BadRequest;
import com.bruno.sistemabancario.domain.exceptions.ResourceNotFoundException;
import com.bruno.sistemabancario.domain.model.BankAccount;
import com.bruno.sistemabancario.domain.model.Transaction;
import com.bruno.sistemabancario.infrastructure.mapper.DozerMapper;
import com.bruno.sistemabancario.infrastructure.repository.BankAccountRepository;
import com.bruno.sistemabancario.infrastructure.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BankService {

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public AccountDTO createAccount(AccountOpeningDTO request) {

        var entity = DozerMapper.parseObject(request, BankAccount.class);
        entity.setName(request.getName());
        entity.setCpf(request.getCpf());
        entity.setOpeningDate(LocalDate.now());
        int randomAccountNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
        entity.setAccountNumber(String.valueOf(randomAccountNumber));
        entity.setBalance(BigDecimal.valueOf(1000));

        return DozerMapper.parseObject(accountRepository.save(entity), AccountDTO.class);
    }

    public BalanceDTO checkBalanceByID(String id) {
        var entity = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this ID!"));

        return new BalanceDTO(entity.getBalance());
    }

    public String moneyTransaction(TransactionDTO request) {
        var source = accountRepository.findByAccountNumber(request.getSourceAccount())
                .orElseThrow(() -> new ResourceNotFoundException("Incorrect account number!"));

        var destination = accountRepository.findByAccountNumber(request.getDestinationAccount())
                .orElseThrow(() -> new ResourceNotFoundException("Incorrect account number!"));

        if (source.getBalance().compareTo(request.getValue()) < 0) {
            throw  new BadRequest("Insufficient source account balance!");
        }

        var transaction = DozerMapper.parseObject(request, Transaction.class);
        transaction.setSourceAccount(request.getSourceAccount());
        transaction.setDestinationAccount(request.getDestinationAccount());
        transaction.setValue(request.getValue());
        transaction.setTransactionDate(LocalDate.now());
        transaction.setStatus("APPROVED");

        transactionRepository.save(transaction);

        source.setBalance(source.getBalance().subtract(request.getValue()));

        accountRepository.save(source);

        destination.setBalance(destination.getBalance().add(request.getValue()));

        accountRepository.save(destination);

        return "Transaction approved successfully!";
    }

    public Page<TransactionsUserDTO> listOfTransactionsSpecificAccount(String accountNumber, Pageable pageable) {
        var account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found!"));

        Page<Transaction> list = transactionRepository.findAllByAccountNumber(accountNumber, pageable);

        if (list.hasContent()) {
            return (Page<TransactionsUserDTO>) list.map(transactions -> {

                return DozerMapper.parseObject(transactions, TransactionsUserDTO.class);
            });
        } else {
            return list.map(transaction -> DozerMapper.parseObject(transaction, TransactionsUserDTO.class));
        }
    }

    public String transactionReversal(String id) {
        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found!"));

        if (transaction.getStatus().contains("APPROVED")) {
            var accountSource = accountRepository.findByAccountNumber(transaction.getSourceAccount())
                    .orElseThrow(() -> new ResourceNotFoundException("Incorrect account number!"));

            var accountDestination = accountRepository.findByAccountNumber(transaction.getDestinationAccount())
                    .orElseThrow(() -> new ResourceNotFoundException("Incorrect account number!"));

            if (accountDestination.getBalance().compareTo(transaction.getValue()) > 0) {

                accountSource.setBalance(accountSource.getBalance().add(transaction.getValue()));
                accountRepository.save(accountSource);

                accountDestination.setBalance(accountDestination.getBalance().subtract(transaction.getValue()));
                accountRepository.save(accountDestination);

                transaction.setStatus("REVERSED");
                transactionRepository.save(transaction);

                return "Transfer successfully reversed!";
            } else {
                throw  new BadRequest("Insufficient destination account balance!");
            }

        } else {
            throw new BadRequest("Transaction is not approved or already reversed.");
        }
    }

    public ReportDTO bankReport() {
        ReportDTO report = new ReportDTO();

        report.setTotalAccounts((int) accountRepository.count());
        report.setTotalTransactions((int) transactionRepository.count());
        report.setTotalTransactionsReversed(BigDecimal.valueOf(transactionRepository.countByStatus("REVERSED")));
        report.setTotalTransactionsApproved(BigDecimal.valueOf(transactionRepository.countByStatus("APPROVED")));
        report.setTotalAmountMoved(getTotalTransactionValue());

        return report;
    }

    private BigDecimal getTotalTransactionValue() {
        var aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").in("APPROVED", "REVERSED")),
                Aggregation.group().sum("value").as("totalValue")
        );

        var result = mongoTemplate.aggregate(aggregation, "transactions", Document.class).getUniqueMappedResult();

        if (result != null) {
            Number totalValueNumber = result.get("totalValue", Number.class);
            if (totalValueNumber != null) {
                return new BigDecimal(totalValueNumber.toString());
            }
        }
        return BigDecimal.ZERO;
    }
}
