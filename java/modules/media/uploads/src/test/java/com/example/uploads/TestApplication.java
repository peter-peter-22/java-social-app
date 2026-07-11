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
@TestPropertySource(locations = {
        "classpath:uploads-test.properties",
        "classpath:object-storage-test.properties"
})
public class TestApplication {
}
