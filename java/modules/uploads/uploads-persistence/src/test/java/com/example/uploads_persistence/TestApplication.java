package com.example.uploads_persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.uploads_persistence",
        "com.example.users_persistence"
})
@EnableJdbcRepositories(basePackages = {
        "com.example.uploads_persistence",
        "com.example.users_persistence"
})
public class TestApplication {
}
