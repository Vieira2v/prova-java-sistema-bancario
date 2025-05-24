package com.bruno.sistemabancario.application.service;

import com.bruno.sistemabancario.adapter.dtos.request.AccountOpeningDTO;
import com.bruno.sistemabancario.adapter.dtos.response.AccountDTO;
import com.bruno.sistemabancario.adapter.dtos.response.BalanceDTO;
import com.bruno.sistemabancario.adapter.dtos.response.ReportDTO;
import com.bruno.sistemabancario.domain.exceptions.ResourceNotFoundException;
import com.bruno.sistemabancario.domain.model.BankAccount;
import com.bruno.sistemabancario.application.ports.input.BankUseCase;
import com.bruno.sistemabancario.application.ports.output.BankAccountRepositoryPort;
import com.bruno.sistemabancario.application.ports.output.TransactionRepositoryPort;
import com.bruno.sistemabancario.domain.utils.Code;
import com.bruno.sistemabancario.domain.utils.CustomMessageResolver;
import com.bruno.sistemabancario.infrastructure.mapper.DozerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BankService implements BankUseCase {

    @Autowired
    private BankAccountRepositoryPort bankAccountRepositoryPort;

    @Autowired
    private TransactionRepositoryPort transactionRepositoryPort;

    @Autowired
    private CustomMessageResolver customMessageResolver;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public AccountDTO createAccount(AccountOpeningDTO request) {

        var entity = DozerMapper.parseObject(request, BankAccount.class);
        entity.setName(request.getName());
        entity.setCpf(request.getCpf());
        entity.setOpeningDate(LocalDate.now());
        int randomAccountNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
        entity.setAccountNumber(String.valueOf(randomAccountNumber));
        entity.setBalance(BigDecimal.valueOf(1000));

        return DozerMapper.parseObject(bankAccountRepositoryPort.save(entity), AccountDTO.class);
    }

    @Override
    public BalanceDTO checkBalanceByID(String id) {
        var entity = bankAccountRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(customMessageResolver.getMessage(Code.NO_ACCOUNT_FOR_ID)));

        return new BalanceDTO(entity.getBalance());
    }

    @Override
    public ReportDTO bankReport() {
        ReportDTO report = new ReportDTO();

        report.setTotalAccounts((int) bankAccountRepositoryPort.count());
        report.setTotalTransactions((int) transactionRepositoryPort.count());
        report.setTotalTransactionsReversed(BigDecimal.valueOf(transactionRepositoryPort.countByStatus("REVERSED")));
        report.setTotalTransactionsApproved(BigDecimal.valueOf(transactionRepositoryPort.countByStatus("APPROVED")));
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
