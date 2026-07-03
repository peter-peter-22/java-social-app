package com.example.media.uploads;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.media.uploads",
        "com.example.users"
})
@EnableJdbcRepositories(basePackages = {
        "com.example.media.uploads",
        "com.example.users"
})
public class TestApplication {
}
