package com.bruno.sistemabancario.application.ports.input;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserUseCase {

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
