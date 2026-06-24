package com.example.cockroach_db;

import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public abstract class CockroachIntegrationTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("deprecation") // the deprecation warning is suppressed because I didn't find the replacement
    private static final CockroachContainer COCKROACH_CONTAINER = new CockroachContainer("cockroachdb/cockroach:v25.3.2");

    @Test
    void testConnection() {
        assertThat(COCKROACH_CONTAINER.isRunning()).isTrue();
    }
}
