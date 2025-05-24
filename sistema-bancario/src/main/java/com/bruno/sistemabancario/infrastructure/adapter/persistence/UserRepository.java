package com.bruno.sistemabancario.infrastructure.adapter.persistence;

import com.bruno.sistemabancario.domain.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);
}
