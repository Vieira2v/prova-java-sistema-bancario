package com.bruno.sistemabancario.application.service;

import com.bruno.sistemabancario.adapter.dtos.request.UserRequest;
import com.bruno.sistemabancario.application.service.security.JwtTokenProvider;
import com.bruno.sistemabancario.application.service.security.dtos.CredentialsLogin;
import com.bruno.sistemabancario.application.service.security.dtos.Token;
import com.bruno.sistemabancario.domain.utils.Code;
import com.bruno.sistemabancario.domain.utils.CustomMessageResolver;
import com.bruno.sistemabancario.infrastructure.config.SecurityConfig;
import com.bruno.sistemabancario.domain.model.User;
import com.bruno.sistemabancario.application.ports.input.AuthenticationUseCase;
import com.bruno.sistemabancario.application.ports.output.UserRepositoryPort;
import com.bruno.sistemabancario.infrastructure.mapper.DozerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements AuthenticationUseCase {

    @Autowired
    private UserRepositoryPort UserRepositoryPort;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomMessageResolver customMessageResolver;

    @Autowired
    private SecurityConfig securityConfig;

    @Override
    public String createUser(UserRequest userRequest) {
        if (userRequest == null) throw  new IllegalArgumentException();

        var user = DozerMapper.parseObject(userRequest, User.class);

        user.setUsername(userRequest.getUsername());
        securityConfig.passwordEncoder().encode(user.getPassword());
        user.setPassword(securityConfig.passwordEncoder().encode(user.getPassword()));

        UserRepositoryPort.save(user);

        return customMessageResolver.getMessage(Code.USER_REGISTERED_SUCCESS);
    }

    @Override
    public Token loginUser(CredentialsLogin login) {
        try {
            var username = login.getUsername();
            var password = login.getPassword();
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            var user = UserRepositoryPort.findByUsername(username);

            var tokenResponse = new Token();
            if (user != null) {
                tokenResponse = jwtTokenProvider.createAccessToken(username);
            } else {
                throw new UsernameNotFoundException(customMessageResolver.getMessage(Code.USERNAME_OR_PASSWORD_INCORRECT));
            }
            return (tokenResponse);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException(customMessageResolver.getMessage(Code.INVALID_USERNAME_OR_PASSWORD));
        }
    }

    @Override
    public Token refreshToken(String username, String refreshToken) {
        var user = UserRepositoryPort.findByUsername(username);

        var tokenResponse = new Token();
        if (user != null) {
            tokenResponse = jwtTokenProvider.createRefreshToken(refreshToken);
        } else {
            throw new UsernameNotFoundException("Username " + username + " not found!");
        }
        return (tokenResponse);
    }
}
