package com.bruno.sistemabancario.infrastructure.adapter.out.persistence;

import com.bruno.sistemabancario.domain.model.User;
import com.bruno.sistemabancario.application.ports.output.UserRepositoryPort;
import com.bruno.sistemabancario.infrastructure.adapter.persistence.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository repository;

    public UserRepositoryAdapter(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return repository.findByUsername(username);
    }
}
