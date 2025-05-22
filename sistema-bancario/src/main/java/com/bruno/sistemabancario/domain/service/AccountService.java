package com.bruno.sistemabancario.domain.service;

import com.bruno.sistemabancario.adapter.dtos.request.AccountOpeningDTO;
import com.bruno.sistemabancario.adapter.dtos.response.AccountDTO;
import com.bruno.sistemabancario.adapter.dtos.response.BalanceDTO;
import com.bruno.sistemabancario.domain.exceptions.ResourceNotFoundException;
import com.bruno.sistemabancario.domain.model.BankAccount;
import com.bruno.sistemabancario.infrastructure.mapper.DozerMapper;
import com.bruno.sistemabancario.infrastructure.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    @Autowired
    private BankAccountRepository repository;

    public AccountDTO createAccount(AccountOpeningDTO request) {
        var entity = DozerMapper.parseObject(request, BankAccount.class);
        entity.setName(request.getName());
        entity.setCPF(request.getCPF());
        entity.setOpeningDate(LocalDate.now());
        int randomAccountNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
        entity.setAccountNumber(String.valueOf(randomAccountNumber));
        entity.setBalance(BigDecimal.valueOf(1000));

        return DozerMapper.parseObject(repository.save(entity), AccountDTO.class);
    }

    public BalanceDTO checkBalanceByID(String id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this ID!"));

        return new BalanceDTO(entity.getBalance());
    }
}
