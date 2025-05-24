package com.bruno.sistemabancario.application.ports.input;

import com.bruno.sistemabancario.adapter.dtos.request.UserRequest;
import com.bruno.sistemabancario.application.service.security.dtos.CredentialsLogin;
import com.bruno.sistemabancario.application.service.security.dtos.Token;

public interface AuthenticationUseCase {

    String createUser(UserRequest userRequest);
    Token loginUser(CredentialsLogin login);
    Token refreshToken(String username, String refreshToken);
}
