package com.bruno.sistemabancario.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import com.bruno.sistemabancario.adapter.dtos.response.TransactionsUserDTO;
import com.bruno.sistemabancario.application.service.PaginationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedModel;

public class PaginationServiceTests {

    private final PaginationService paginationService = new PaginationService();

    @Test
    void testFindAllTransactionByAccountWithNextAndPreviousLinks() {
        List<TransactionsUserDTO> content = Arrays.asList(
                new TransactionsUserDTO(),
                new TransactionsUserDTO()
        );

        Page<TransactionsUserDTO> page = new PageImpl<>(
                content,
                PageRequest.of(1, 2),
                5
        );

        String accountNumber = "12345";
        int currentPage = 1;
        int size = 2;

        PagedModel<TransactionsUserDTO> pagedModel = paginationService.findAllTransactionByAccount(page, accountNumber, currentPage, size);

        assertThat(pagedModel.getContent()).hasSize(2);

        PagedModel.PageMetadata metadata = pagedModel.getMetadata();
        Assertions.assertNotNull(metadata);
        assertThat(metadata.getSize()).isEqualTo(2);
        assertThat(metadata.getNumber()).isEqualTo(1);
        assertThat(metadata.getTotalElements()).isEqualTo(5);

        assertThat(pagedModel.getLinks()).isNotEmpty();

        assertThat(pagedModel.getLink("self")).isPresent();
        assertThat(pagedModel.getLink("next")).isPresent();
        assertThat(pagedModel.getLink("previous")).isPresent();

        String selfHref = pagedModel.getLink("self").get().getHref();
        String nextHref = pagedModel.getLink("next").get().getHref();
        String prevHref = pagedModel.getLink("previous").get().getHref();

        assertThat(selfHref).contains(accountNumber);
        assertThat(nextHref).contains("page=2");
        assertThat(prevHref).contains("page=0");
    }

    @Test
    void testFindAllTransactionByAccountOnlySelfLinkWhenNoNextOrPrevious() {
        List<TransactionsUserDTO> content = List.of(new TransactionsUserDTO());

        Page<TransactionsUserDTO> page = new PageImpl<>(
                content,
                PageRequest.of(0, 1),
                1
        );

        String accountNumber = "12345";
        int currentPage = 0;
        int size = 1;

        PagedModel<TransactionsUserDTO> pagedModel = paginationService.findAllTransactionByAccount(page, accountNumber, currentPage, size);

        assertThat(pagedModel.getLink("self")).isPresent();
        assertThat(pagedModel.getLink("next")).isNotPresent();
        assertThat(pagedModel.getLink("previous")).isNotPresent();
    }
}
