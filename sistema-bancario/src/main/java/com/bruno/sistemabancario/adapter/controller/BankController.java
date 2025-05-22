package com.bruno.sistemabancario.adapter.controller;

import com.bruno.sistemabancario.adapter.dtos.request.AccountOpeningDTO;
import com.bruno.sistemabancario.adapter.dtos.response.AccountDTO;
import com.bruno.sistemabancario.adapter.dtos.response.BalanceDTO;
import com.bruno.sistemabancario.domain.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/api/banking/system")
public class BankController {

    @Autowired
    private AccountService accountService;

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
    public ResponseEntity<AccountDTO> createAccount(@RequestBody AccountOpeningDTO request) {
        var created = accountService.createAccount(request);

        return ResponseEntity.ok(created);
    }

    @Operation(summary="Check balance",
            description="Check balance by account ID",
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
    @GetMapping("/balance/{id}")
    public ResponseEntity<BalanceDTO> balanceByID(@PathVariable(value = "id") String id) {
        return  ResponseEntity.ok(accountService.checkBalanceByID(id));
    }
}
