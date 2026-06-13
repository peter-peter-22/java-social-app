package com.example.users;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.users.repository.InsertUser;
import com.example.users.repository.User;
import com.example.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class UserRepositoryIT extends CockroachIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private static final InsertUser insert = new InsertUser();

    @Test
    void createUser() {
        // The created user should be returned
        var created = userRepository.create(insert);
        assertThat(created).isNotNull();
    }

    @Test
    void readUser() {
        // The inserted user should be found
        var created = userRepository.create(insert);
        var found = userRepository.findById(created.id());
        assertThat(found).isNotNull();
        assertThat(Objects.equals(found.id(), created.id()));
    }

    @Test
    void updateUser() {
        // The update should change the data of the user
        var created = userRepository.create(insert);
        var update = new User(
                created.id()
        );
        userRepository.update(update);
        var updated = userRepository.findById(created.id());

        assertThat(updated).isNotNull();
        assertThat(Objects.equals(updated.id(), update.id()));
    }
}
