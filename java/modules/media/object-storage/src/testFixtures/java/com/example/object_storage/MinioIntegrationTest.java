package com.example.object_storage;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

@TestPropertySource(locations = "classpath:object-storage-test.properties")
public abstract class MinioIntegrationTest {

    private static final MinIOContainer MINIO_CONTAINER = startContainer();

    private static MinIOContainer startContainer() {
        var container = new MinIOContainer(DockerImageName
                .parse("docker.io/pgsty/silo:RELEASE.2026-11-12T00-00-00Z")
                .asCompatibleSubstituteFor("minio/minio"));
        container.start();
        return container;
    }

    /**
     * The container is shared by all integration-test classes in a test JVM.
     * Using {@code @Container} here would make the JUnit extension stop this
     * shared container after the first test class.
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.regions[0].name", () -> "test");
        registry.add("minio.regions[0].endpoint", MINIO_CONTAINER::getS3URL);
        registry.add("minio.regions[0].access-key", MINIO_CONTAINER::getUserName);
        registry.add("minio.regions[0].secret-key", MINIO_CONTAINER::getPassword);
    }
}
