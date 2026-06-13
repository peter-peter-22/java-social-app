package com.example.uploads;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication( scanBasePackages = {
        "com.example.users",
        "com.example.uploads"
})
@EnableJdbcRepositories(basePackages = {
        "com.example.uploads",
        "com.example.users"
})
public class TestApplication {
}
