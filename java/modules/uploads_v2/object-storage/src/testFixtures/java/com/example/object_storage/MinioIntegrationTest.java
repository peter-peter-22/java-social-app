package com.example.object_storage;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@TestPropertySource(locations = "classpath:object-storage-test.properties")
public abstract class MinioIntegrationTest {
    @Container
    private static final MinIOContainer MINIO_CONTAINER =
            new MinIOContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.regions[0].name", () -> "test");
        registry.add("minio.regions[0].endpoint", MINIO_CONTAINER::getS3URL);
        registry.add("minio.regions[0].access-key", MINIO_CONTAINER::getUserName);
        registry.add("minio.regions[0].secret-key", MINIO_CONTAINER::getPassword);
    }

    @Test
    void testConnection() {
        assertThat(MINIO_CONTAINER.isRunning()).isTrue();
    }
}
