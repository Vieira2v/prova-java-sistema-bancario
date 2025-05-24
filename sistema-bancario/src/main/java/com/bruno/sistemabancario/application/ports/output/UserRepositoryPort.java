package com.bruno.sistemabancario.application.ports.output;

import com.bruno.sistemabancario.domain.model.User;

public interface UserRepositoryPort {

    User save(User user);
    User findByUsername(String username);
}
