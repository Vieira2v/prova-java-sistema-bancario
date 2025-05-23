package com.bruno.sistemabancario;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.bruno.sistemabancario.adapter.dtos.request.UserRequest;
import com.bruno.sistemabancario.config.SecurityConfig;
import com.bruno.sistemabancario.domain.model.User;
import com.bruno.sistemabancario.domain.service.AuthenticationService;
import com.bruno.sistemabancario.domain.service.security.JwtTokenProvider;
import com.bruno.sistemabancario.domain.service.security.dtos.CredentialsLogin;
import com.bruno.sistemabancario.domain.service.security.dtos.Token;
import com.bruno.sistemabancario.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class UserServiceTests {

    @Mock
    private UserRepository repository;

    @Mock
    private SecurityConfig securityConfig;

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

        ResponseEntity<String> response = authenticationService.createUser(userRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully!", response.getBody());

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

        ResponseEntity<Token> response = authenticationService.loginUser(login);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(token, response.getBody());
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

        ResponseEntity<Token> response = authenticationService.refreshToken(username, refreshToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(token, response.getBody());
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
