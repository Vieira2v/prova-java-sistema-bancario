package com.bruno.sistemabancario.repositories;

import com.bruno.sistemabancario.domain.model.User;
import com.bruno.sistemabancario.infrastructure.adapter.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        User user = new User();
        user.setId("1");
        user.setUsername("bruno");
        user.setPassword("123456");

        userRepository.save(user);
    }

    @Test
    void findByUsernamethenReturnUser() {
        User found = userRepository.findByUsername("bruno");

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("bruno");
    }

    @Test
    void whenFindByUsernameNotExists_thenReturnNull() {
        User found = userRepository.findByUsername("naoexiste");

        assertThat(found).isNull();
    }
}
