package com.bruno.sistemabancario.domain.service;

import com.bruno.sistemabancario.adapter.dtos.request.UserRequest;
import com.bruno.sistemabancario.config.SecurityConfig;
import com.bruno.sistemabancario.domain.model.User;
import com.bruno.sistemabancario.domain.service.security.JwtTokenProvider;
import com.bruno.sistemabancario.domain.service.security.dtos.CredentialsLogin;
import com.bruno.sistemabancario.domain.service.security.dtos.Token;
import com.bruno.sistemabancario.infrastructure.mapper.DozerMapper;
import com.bruno.sistemabancario.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private SecurityConfig securityConfig;

    public ResponseEntity<String> createUser(UserRequest userRequest) {
        if (userRequest == null) throw  new IllegalArgumentException();

        var user = DozerMapper.parseObject(userRequest, User.class);

        user.setUsername(userRequest.getUsername());
        securityConfig.passwordEncoder().encode(user.getPassword());
        user.setPassword(securityConfig.passwordEncoder().encode(user.getPassword()));

        repository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }

    public ResponseEntity loginUser(CredentialsLogin login) {
        try {
            var username = login.getUsername();
            var password = login.getPassword();
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            var user = repository.findByUsername(username);

            var tokenResponse = new Token();
            if (user != null) {
                tokenResponse = jwtTokenProvider.createAccessToken(username);
            } else {
                throw new UsernameNotFoundException("Username or password is incorrect");
            }
            return ResponseEntity.ok(tokenResponse);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public ResponseEntity refreshToken(String username, String refreshToken) {
        var user = repository.findByUsername(username);

        var tokenResponse = new Token();
        if (user != null) {
            tokenResponse = jwtTokenProvider.createRefreshToken(refreshToken);
        } else {
            throw new UsernameNotFoundException("Username " + username + " not found!");
        }
        return ResponseEntity.ok(tokenResponse);
    }
}
