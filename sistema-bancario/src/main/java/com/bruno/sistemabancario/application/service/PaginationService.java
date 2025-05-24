package com.bruno.sistemabancario.application.service;

import com.bruno.sistemabancario.adapter.controller.BankController;
import com.bruno.sistemabancario.adapter.dtos.response.TransactionsUserDTO;
import com.bruno.sistemabancario.application.ports.input.PaginationUseCase;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;

@Service
public class PaginationService implements PaginationUseCase {

    @Override
    public PagedModel<TransactionsUserDTO> findAllTransactionByAccount(Page<TransactionsUserDTO> transactionPage, String accountNumber, int page, int size) {
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                transactionPage.getSize(),
                transactionPage.getNumber(),
                transactionPage.getTotalElements()
        );

        PagedModel<TransactionsUserDTO> pagedModel = PagedModel.of(transactionPage.getContent(), metadata);

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                        .methodOn(BankController.class)
                        .searchAllTransactionsByAccountNumber(accountNumber, page, size))
                .withSelfRel();
        pagedModel.add(selfLink);

        if (transactionPage.hasNext()) {
            Link nextLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                            .methodOn(BankController.class)
                            .searchAllTransactionsByAccountNumber(accountNumber, page + 1, size))
                    .withRel("next");
            pagedModel.add(nextLink);
        }

        if (transactionPage.hasPrevious()) {
            Link prevLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                            .methodOn(BankController.class)
                            .searchAllTransactionsByAccountNumber(accountNumber, page - 1, size))
                    .withRel("previous");
            pagedModel.add(prevLink);
        }

        return pagedModel;
    }
}
