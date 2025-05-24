package com.bruno.sistemabancario.application.service;

import com.bruno.sistemabancario.adapter.dtos.request.TransactionDTO;
import com.bruno.sistemabancario.adapter.dtos.response.TransactionsUserDTO;
import com.bruno.sistemabancario.application.ports.input.TransferUseCase;
import com.bruno.sistemabancario.application.ports.output.BankAccountRepositoryPort;
import com.bruno.sistemabancario.application.ports.output.TransactionRepositoryPort;
import com.bruno.sistemabancario.domain.exceptions.BadRequest;
import com.bruno.sistemabancario.domain.exceptions.ResourceNotFoundException;
import com.bruno.sistemabancario.domain.model.Transaction;
import com.bruno.sistemabancario.domain.utils.Code;
import com.bruno.sistemabancario.domain.utils.CustomMessageResolver;
import com.bruno.sistemabancario.infrastructure.mapper.DozerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class TransferService implements TransferUseCase {

    @Autowired
    private BankAccountRepositoryPort bankAccountRepositoryPort;

    @Autowired
    private TransactionRepositoryPort transactionRepositoryPort;

    @Autowired
    private CustomMessageResolver customMessageResolver;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String moneyTransaction(TransactionDTO request) {

        if (request.getValue() == null || request.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequest(customMessageResolver.getMessage(Code.INVALID_TRANSACTION_VALUE));
        }

        if (request.getSourceAccount() == null || request.getDestinationAccount() == null) {
            throw new BadRequest(customMessageResolver.getMessage(Code.INVALID_ACCOUNT_NUMBER));
        }

        if (request.getSourceAccount().equals(request.getDestinationAccount())) {
            throw new BadRequest(customMessageResolver.getMessage(Code.SOURCE_AND_DESTINATION_SAME));
        }

        var source = bankAccountRepositoryPort.findByAccountNumber(request.getSourceAccount())
                .orElseThrow(() -> new ResourceNotFoundException(customMessageResolver.getMessage(Code.ACCOUNT_NOT_FOUND)));

        var destination = bankAccountRepositoryPort.findByAccountNumber(request.getDestinationAccount())
                .orElseThrow(() -> new ResourceNotFoundException(customMessageResolver.getMessage(Code.ACCOUNT_NOT_FOUND)));

        if (source.getBalance().compareTo(request.getValue()) < 0) {
            throw  new BadRequest(customMessageResolver.getMessage(Code.INSUFFICIENT_BALANCE));
        }

        var transaction = DozerMapper.parseObject(request, Transaction.class);
        transaction.setSourceAccount(request.getSourceAccount());
        transaction.setDestinationAccount(request.getDestinationAccount());
        transaction.setValue(request.getValue());
        transaction.setTransactionDate(LocalDate.now());
        transaction.setStatus("APPROVED");

        transactionRepositoryPort.save(transaction);

        source.setBalance(source.getBalance().subtract(request.getValue()));

        bankAccountRepositoryPort.save(source);

        destination.setBalance(destination.getBalance().add(request.getValue()));

        bankAccountRepositoryPort.save(destination);

        return customMessageResolver.getMessage(Code.TRANSACTION_APPROVED_SUCCESS);
    }

    @Override
    public Page<TransactionsUserDTO> listOfTransactionsSpecificAccount(String accountNumber, Pageable pageable) {
        var account = bankAccountRepositoryPort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(customMessageResolver.getMessage(Code.NUMBER_ACCOUNT_NOT_FOUND)));

        Page<Transaction> list = transactionRepositoryPort.findAllByAccountNumber(accountNumber, pageable);

        if (list.hasContent()) {
            return (Page<TransactionsUserDTO>) list.map(transactions -> {

                return DozerMapper.parseObject(transactions, TransactionsUserDTO.class);
            });
        } else {
            return list.map(transaction -> DozerMapper.parseObject(transaction, TransactionsUserDTO.class));
        }
    }

    @Override
    public String transactionReversal(String id) {
        var transaction = transactionRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(customMessageResolver.getMessage(Code.TRANSACTION_NOT_FOUND)));

        if (transaction.getStatus().contains("APPROVED")) {
            var accountSource = bankAccountRepositoryPort.findByAccountNumber(transaction.getSourceAccount())
                    .orElseThrow(() -> new ResourceNotFoundException(customMessageResolver.getMessage(Code.ACCOUNT_NOT_FOUND)));

            var accountDestination = bankAccountRepositoryPort.findByAccountNumber(transaction.getDestinationAccount())
                    .orElseThrow(() -> new ResourceNotFoundException(customMessageResolver.getMessage(Code.ACCOUNT_NOT_FOUND)));

            if (accountDestination.getBalance().compareTo(transaction.getValue()) > 0) {

                accountSource.setBalance(accountSource.getBalance().add(transaction.getValue()));
                bankAccountRepositoryPort.save(accountSource);

                accountDestination.setBalance(accountDestination.getBalance().subtract(transaction.getValue()));
                bankAccountRepositoryPort.save(accountDestination);

                transaction.setStatus("REVERSED");
                transactionRepositoryPort.save(transaction);

                return customMessageResolver.getMessage(Code.TRANSACTION_REVERSED_SUCCESS);
            } else {
                throw  new BadRequest(customMessageResolver.getMessage(Code.DESTINATION_ACCOUNT_INSUFFICIENT_BALANCE));
            }

        } else {
            throw new BadRequest(customMessageResolver.getMessage(Code.TRANSACTION_NOT_APPROVED));
        }
    }
}
