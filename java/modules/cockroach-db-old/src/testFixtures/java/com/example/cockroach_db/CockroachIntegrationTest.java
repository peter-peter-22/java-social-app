package com.example.cockroach_db;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.cockroachdb.CockroachContainer;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(locations = "classpath:database-test.properties")
public abstract class CockroachIntegrationTest {

    private static final CockroachContainer COCKROACH_CONTAINER = startContainer();

    private static CockroachContainer startContainer() {
        var container = new CockroachContainer("cockroachdb/cockroach:v25.3.2");
        container.start();
        return container;
    }

    /**
     * The container is shared by all integration-test classes in a test JVM.
     * Using {@code @Container} here would make the JUnit extension stop this
     * shared container after the first test class.
     */
    @DynamicPropertySource
    static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", COCKROACH_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", COCKROACH_CONTAINER::getUsername);
        registry.add("spring.datasource.password", COCKROACH_CONTAINER::getPassword);
    }

    @Test
    void testConnection() {
        assertThat(COCKROACH_CONTAINER.isRunning()).isTrue();
    }
}
