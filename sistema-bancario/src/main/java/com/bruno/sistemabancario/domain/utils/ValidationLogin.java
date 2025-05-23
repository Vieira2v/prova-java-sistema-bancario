package com.bruno.sistemabancario.domain.utils;

import com.bruno.sistemabancario.domain.service.security.dtos.CredentialsLogin;
import org.springframework.stereotype.Component;

@Component
public class ValidationLogin {

    public boolean checkIfParamsIsNotNull(CredentialsLogin data) {
        return data == null || data.getUsername() == null || data.getUsername().isBlank() || data.getPassword() == null || data.getPassword().isBlank();
    }

    public boolean checkIfParamsIsNotNull(String username, String refreshToken) {
        return refreshToken == null || refreshToken.isBlank() ||
                username == null || username.isBlank();
    }
}
