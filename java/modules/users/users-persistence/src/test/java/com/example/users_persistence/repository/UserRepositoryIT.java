package com.example.users_persistence.repository;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.users.api.repository.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserRepositoryIT extends CockroachIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private static final InsertUser insert = new InsertUser();

    /** The created user should be returned */
    @Test
    void createUser() {
        var created = userRepository.create(insert);
        assertThat(created).isNotNull();
    }

    /** The inserted user should be found */
    @Test
    void readUser() {
        var created = userRepository.create(insert);
        var found = userRepository.findById(created.id());
        assertThat(found).isNotNull();
        assertThat(Objects.equals(found.id(), created.id()));
    }

    /** The update should change the data of the user */
    @Test
    void updateUser() {
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
