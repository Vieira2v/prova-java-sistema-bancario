package com.bruno.sistemabancario.application.ports.input;

import com.bruno.sistemabancario.adapter.dtos.request.AccountOpeningDTO;
import com.bruno.sistemabancario.adapter.dtos.response.AccountDTO;
import com.bruno.sistemabancario.adapter.dtos.response.BalanceDTO;
import com.bruno.sistemabancario.adapter.dtos.response.ReportDTO;

public interface BankUseCase {

    AccountDTO createAccount(AccountOpeningDTO request);

    BalanceDTO checkBalanceByID(String id);

    ReportDTO bankReport();
}
