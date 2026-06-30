package com.example.object_storage;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public abstract class MinioIntegrationTest {
    @Container
    private static final MinIOContainer MINIO_CONTAINER =
            new MinIOContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", MINIO_CONTAINER::getS3URL);
        registry.add("minio.access-key", MINIO_CONTAINER::getUserName);
        registry.add("minio.secret-key", MINIO_CONTAINER::getPassword);
        registry.add("minio.reconciliation.enabled", () -> "true");
    }

    @Test
    void testConnection() {
        assertThat(MINIO_CONTAINER.isRunning()).isTrue();
    }
}
