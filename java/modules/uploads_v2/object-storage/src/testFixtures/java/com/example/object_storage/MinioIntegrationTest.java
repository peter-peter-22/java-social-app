package com.example.object_storage;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@TestPropertySource(locations = "classpath:object-storage-test.properties")
public abstract class MinioIntegrationTest {
    @Container
    private static final MinIOContainer MINIO_CONTAINER =
            new MinIOContainer(DockerImageName
                    .parse("docker.io/pgsty/silo:RELEASE.2026-11-12T00-00-00Z")
                    .asCompatibleSubstituteFor("minio/minio"));

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
