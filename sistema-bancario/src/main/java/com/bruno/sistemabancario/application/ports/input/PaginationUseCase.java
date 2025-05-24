package com.bruno.sistemabancario.application.ports.input;

import com.bruno.sistemabancario.adapter.dtos.response.TransactionsUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;

public interface PaginationUseCase {

    PagedModel<TransactionsUserDTO> findAllTransactionByAccount(Page<TransactionsUserDTO> transactionPage, String accountNumber, int page, int size);
}
