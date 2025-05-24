package com.bruno.sistemabancario.application.service;

import com.bruno.sistemabancario.application.ports.input.UserUseCase;
import com.bruno.sistemabancario.application.ports.output.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class UserService implements UserDetailsService, UserUseCase {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info(String.format("Finding one user by name %s!", username));
        var user = userRepositoryPort.findByUsername(username);

        if (user != null) {
            return user;
        }else {
            throw new UsernameNotFoundException("Username "+ username + " not found!");
        }
    }
}
