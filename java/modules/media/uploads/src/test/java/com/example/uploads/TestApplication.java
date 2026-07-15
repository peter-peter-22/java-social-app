package com.example.uploads;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.test.context.TestPropertySource;

@SpringBootApplication(scanBasePackages = {
        "com.example.uploads",
        "com.example.users_persistence"
})
@EnableJdbcRepositories(basePackages = {
        "com.example.uploads",
        "com.example.users_persistence"
})
public class TestApplication {
}
