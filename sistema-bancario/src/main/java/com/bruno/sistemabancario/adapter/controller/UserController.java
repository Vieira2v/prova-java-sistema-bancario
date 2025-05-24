package com.bruno.sistemabancario.adapter.controller;

import com.bruno.sistemabancario.adapter.dtos.request.UserRequest;
import com.bruno.sistemabancario.application.ports.input.AuthenticationUseCase;
import com.bruno.sistemabancario.application.service.security.dtos.CredentialsLogin;
import com.bruno.sistemabancario.domain.utils.ValidationLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/api/banking")
public class UserController {

    @Autowired
    private ValidationLogin validationLogin;

    @Autowired
    private AuthenticationUseCase authenticationUseCase;

    @Operation(summary="Register user",
            description="Register user",
            tags={"User"},
            responses={
                    @ApiResponse(description="Success", responseCode="200",
                            content={
                                    @Content(
                                            mediaType="application/json",
                                            array=@ArraySchema(schema=@Schema(implementation= UserRequest.class))
                                    )
                            }),
                    @ApiResponse(description="Bad Request", responseCode="400", content=@Content),
                    @ApiResponse(description="Unauthorized", responseCode="401", content=@Content),
                    @ApiResponse(description="Not Found", responseCode="404", content=@Content),
                    @ApiResponse(description="Internal Error", responseCode="500", content=@Content)
            })
    @PostMapping(value = "/register")
    public ResponseEntity registerUser(@RequestBody UserRequest userRequest) {
        var create = authenticationUseCase.createUser(userRequest);
        return ResponseEntity.ok(create);
    }

    @Operation(summary="Login user",
            description="Login user",
            tags={"User"},
            responses={
                    @ApiResponse(description="Success", responseCode="200",
                            content={
                                    @Content(
                                            mediaType="application/json",
                                            array=@ArraySchema(schema=@Schema(implementation= UserRequest.class))
                                    )
                            }),
                    @ApiResponse(description="Bad Request", responseCode="400", content=@Content),
                    @ApiResponse(description="Unauthorized", responseCode="401", content=@Content),
                    @ApiResponse(description="Not Found", responseCode="404", content=@Content),
                    @ApiResponse(description="Internal Error", responseCode="500", content=@Content)
            })
    @SuppressWarnings("rawtypes")
    @PostMapping(value = "/login")
    public ResponseEntity login(@RequestBody CredentialsLogin credentialsLogin) {

        if (validationLogin.checkIfParamsIsNotNull(credentialsLogin)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }

        return ResponseEntity.ok(authenticationUseCase.loginUser(credentialsLogin));
    }

    @Operation(summary="Refresh token for user.",
            description="Refresh token for authenticated user and returns a token",
            tags={"User"},
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
    @SuppressWarnings("rawtypes")
    @PutMapping(value = "/refresh/{username}")
    public ResponseEntity refreshToken(@PathVariable("username") String username,
                                       @RequestHeader("Authorization") String refreshToken) {

        if (validationLogin.checkIfParamsIsNotNull(username, refreshToken))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");

        var token = authenticationUseCase.refreshToken(username, refreshToken);

        if (token == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");

        return ResponseEntity.ok(token);
    }

}
