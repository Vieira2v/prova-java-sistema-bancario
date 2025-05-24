package com.bruno.sistemabancario.application.ports.input;

import com.bruno.sistemabancario.adapter.dtos.request.TransactionDTO;
import com.bruno.sistemabancario.adapter.dtos.response.TransactionsUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferUseCase {

    String moneyTransaction(TransactionDTO request);
    Page<TransactionsUserDTO> listOfTransactionsSpecificAccount(String accountNumber, Pageable pageable);
    String transactionReversal(String id);

}
