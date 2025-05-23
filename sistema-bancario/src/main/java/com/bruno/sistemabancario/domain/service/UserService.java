package com.bruno.sistemabancario.domain.service;

import com.bruno.sistemabancario.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info(String.format("Finding one user by name %s!", username));
        var user = repository.findByUsername(username);

        if (user != null) {
            return user;
        }else {
            throw new UsernameNotFoundException("Username "+ username + " not found!");
        }
    }
}
