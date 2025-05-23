package com.bruno.sistemabancario.adapter.controller;

import com.bruno.sistemabancario.adapter.dtos.request.AccountOpeningDTO;
import com.bruno.sistemabancario.adapter.dtos.request.TransactionDTO;
import com.bruno.sistemabancario.adapter.dtos.response.AccountDTO;
import com.bruno.sistemabancario.adapter.dtos.response.BalanceDTO;
import com.bruno.sistemabancario.adapter.dtos.response.ReportDTO;
import com.bruno.sistemabancario.adapter.dtos.response.TransactionsUserDTO;
import com.bruno.sistemabancario.domain.service.BankService;
import com.bruno.sistemabancario.domain.service.PaginationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/api/banking/system")
public class BankController {

    @Autowired
    private BankService bankService;

    @Autowired
    private PaginationService paginationService;

    @Operation(summary="Create account",
            description="Create account",
            tags={"Banking System"},
            responses={
                    @ApiResponse(description="Success", responseCode="200",
                            content={
                                    @Content(
                                            mediaType="application/json",
                                            array=@ArraySchema(schema=@Schema(implementation= AccountOpeningDTO.class))
                                    )
                            }),
                    @ApiResponse(description="Bad Request", responseCode="400", content=@Content),
                    @ApiResponse(description="Unauthorized", responseCode="401", content=@Content),
                    @ApiResponse(description="Not Found", responseCode="404", content=@Content),
                    @ApiResponse(description="Internal Error", responseCode="500", content=@Content)
            })
    @PostMapping("/register")
    public ResponseEntity<AccountDTO> createAccount(@RequestBody @Valid AccountOpeningDTO request) {
        var created = bankService.createAccount(request);

        return ResponseEntity.ok(created);
    }

    @Operation(summary="Check balance",
            description="Check balance by account ID",
            tags={"Banking System"},
            responses={
                    @ApiResponse(description="Success", responseCode="200",
                            content={
                                    @Content(
                                            mediaType="application/json",
                                            array=@ArraySchema(schema=@Schema(implementation= BalanceDTO.class))
                                    )
                            }),
                    @ApiResponse(description="Bad Request", responseCode="400", content=@Content),
                    @ApiResponse(description="Unauthorized", responseCode="401", content=@Content),
                    @ApiResponse(description="Not Found", responseCode="404", content=@Content),
                    @ApiResponse(description="Internal Error", responseCode="500", content=@Content)
            })
    @GetMapping("/balance/{id}")
    public ResponseEntity<BalanceDTO> balanceByID(@PathVariable(value = "id") String id) {
        return  ResponseEntity.ok(bankService.checkBalanceByID(id));
    }

    @Operation(summary="Transaction",
            description="Make a transaction to another account",
            tags={"Banking System"},
            responses={
                    @ApiResponse(description="Success", responseCode="200",
                            content={
                                    @Content(
                                            mediaType="application/json"
                                    )
                            }),
                    @ApiResponse(description="Bad Request", responseCode="400", content=@Content),
                    @ApiResponse(description="Unauthorized", responseCode="401", content=@Content),
                    @ApiResponse(description="Not Found", responseCode="404", content=@Content),
                    @ApiResponse(description="Internal Error", responseCode="500", content=@Content)
            })
    @PostMapping("/transaction")
    public ResponseEntity<String> makeTransaction(@RequestBody @Valid TransactionDTO request) {
        var transaction = bankService.moneyTransaction(request);

        return ResponseEntity.ok(transaction);
    }

    @Operation(summary="Transaction List",
            description="List of transactions for a given account",
            tags={"Banking System"},
            responses={
                    @ApiResponse(description="Success", responseCode="200",
                            content={
                                    @Content(
                                            mediaType="application/json",
                                            array=@ArraySchema(schema=@Schema(implementation= TransactionsUserDTO.class))
                                    )
                            }),
                    @ApiResponse(description="Bad Request", responseCode="400", content=@Content),
                    @ApiResponse(description="Unauthorized", responseCode="401", content=@Content),
                    @ApiResponse(description="Not Found", responseCode="404", content=@Content),
                    @ApiResponse(description="Internal Error", responseCode="500", content=@Content)
            })
    @GetMapping("/transactions/{accountNumber}")
    public ResponseEntity<PagedModel<TransactionsUserDTO>> searchAllTransactionsByAccountNumber(@PathVariable(value = "accountNumber") String accountNumber,
                                                                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                                @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionsUserDTO> responsePage = bankService.listOfTransactionsSpecificAccount(accountNumber, pageable);

        PagedModel<TransactionsUserDTO> pagedModel = paginationService.findAllTransactionByAccount(responsePage, accountNumber, page, size);
        return  ResponseEntity.ok(pagedModel);
    }

    @Operation(summary="Reverse transaction",
            description="Reverse transaction by transaction ID",
            tags={"Banking System"},
            responses={
                    @ApiResponse(description="Success", responseCode="200",
                            content={
                                    @Content(
                                            mediaType="application/json"
                                    )
                            }),
                    @ApiResponse(description="Bad Request", responseCode="400", content=@Content),
                    @ApiResponse(description="Unauthorized", responseCode="401", content=@Content),
                    @ApiResponse(description="Not Found", responseCode="404", content=@Content),
                    @ApiResponse(description="Internal Error", responseCode="500", content=@Content)
            })
    @PostMapping("/reversed/transaction/{id}")
    public ResponseEntity<String> reverseTransfer(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(bankService.transactionReversal(id));
    }

    @Operation(summary="Report",
            description="Bank Report",
            tags={"Banking System"},
            responses={
                    @ApiResponse(description="Success", responseCode="200",
                            content={
                                    @Content(
                                            mediaType="application/json"
                                    )
                            }),
                    @ApiResponse(description="Bad Request", responseCode="400", content=@Content),
                    @ApiResponse(description="Unauthorized", responseCode="401", content=@Content),
                    @ApiResponse(description="Not Found", responseCode="404", content=@Content),
                    @ApiResponse(description="Internal Error", responseCode="500", content=@Content)
            })
    @GetMapping("/report")
    public ReportDTO reportBank() {
        return bankService.bankReport();
    }
}
