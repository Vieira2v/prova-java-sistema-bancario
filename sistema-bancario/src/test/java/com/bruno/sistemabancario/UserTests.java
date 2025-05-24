package com.bruno.sistemabancario;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.bruno.sistemabancario.adapter.dtos.request.UserRequest;
import com.bruno.sistemabancario.application.ports.output.UserRepositoryPort;
import com.bruno.sistemabancario.application.service.security.JwtTokenProvider;
import com.bruno.sistemabancario.application.service.security.dtos.CredentialsLogin;
import com.bruno.sistemabancario.application.service.security.dtos.Token;
import com.bruno.sistemabancario.domain.utils.Code;
import com.bruno.sistemabancario.domain.utils.CustomMessageResolver;
import com.bruno.sistemabancario.infrastructure.config.SecurityConfig;
import com.bruno.sistemabancario.domain.model.User;
import com.bruno.sistemabancario.application.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class UserTests {

    @Mock
    private UserRepositoryPort repository;

    @Mock
    private SecurityConfig securityConfig;

    @Mock
    private CustomMessageResolver customMessageResolver;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void UserServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUserSuccess() {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("bruno");
        userRequest.setPassword("123456");

        User userMapped = new User();
        userMapped.setUsername(userRequest.getUsername());
        userMapped.setPassword(userRequest.getPassword());

        when(securityConfig.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");

        when(repository.save(any(User.class))).thenReturn(userMapped);
        when(customMessageResolver.getMessage(Code.USER_REGISTERED_SUCCESS))
                .thenReturn("User registered successfully!");

        String response = authenticationService.createUser(userRequest);

        assertEquals("User registered successfully!", response);

        verify(repository).save(argThat(user ->
                user.getUsername().equals("bruno") && user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void testCreateUserNullUserException() {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("");
        userRequest.setPassword("123456");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createUser(null);
        });

        assertEquals(IllegalArgumentException.class, exception.getClass());
    }

    @Test
    void testLoginUserSuccess() {
        CredentialsLogin login = new CredentialsLogin();
        login.setUsername("user1");
        login.setPassword("password1");

        User user = new User();
        user.setUsername("user1");

        Token token = new Token();
        token.setAccessToken("jwt-token");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        when(repository.findByUsername("user1")).thenReturn(user);

        when(jwtTokenProvider.createAccessToken("user1")).thenReturn(token);

        Token response = authenticationService.loginUser(login);

        assertEquals(token, response);
        assertEquals(token, response);
    }

    @Test
    void testLoginUserError() {
        CredentialsLogin login = new CredentialsLogin();
        login.setUsername("user1");
        login.setPassword("wrongpassword");

        when(repository.findByUsername(login.getUsername())).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.loginUser(login);
        });
    }

    @Test
    void testRefreshTokenSuccess() {
        String username = "user1";
        String refreshToken = "valid-refresh-token";

        User user = new User();
        user.setUsername(username);

        Token token = new Token();
        token.setRefreshToken("new-refresh-token");

        when(repository.findByUsername(username)).thenReturn(user);
        when(jwtTokenProvider.createRefreshToken(refreshToken)).thenReturn(token);

        Token response = authenticationService.refreshToken(username, refreshToken);

        assertEquals(token, response);
        assertEquals(token, response);
    }

    @Test
    void testRefreshTokenUserException() {
        String username = "user1";
        String refreshToken = "valid-refresh-token";

        when(repository.findByUsername(username)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.refreshToken(username, refreshToken);
        });
    }

}
