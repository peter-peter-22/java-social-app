package com.example.object_storage.regional;

import com.example.object_storage.MinioIntegrationTest;
import com.example.object_storage.regional.args.*;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@NullMarked
public class RegionalObjectRepositoriesIT extends MinioIntegrationTest {
    private static final String BUCKET = "private";
    private static final String REGION = "test";
    private static final String CONTENT_TYPE = "text/plain";

    @Autowired
    private RegionalObjectRepositories regionalObjectRepositories;

    @TempDir
    Path tempDir;

    @Test
        // Short test for CRUD operations on the local object storage.
    void managesObjectsInTheHomeRegion() throws IOException {
        var repository = regionalObjectRepositories.getLocal();
        var source = new ClassPathResource("test.txt").getFile().toPath();
        var content = Files.readAllBytes(source);
        var uploadedKey = randomObjectKey();
        var putKey = randomObjectKey();

        assertThat(repository).isSameAs(regionalObjectRepositories.getByRegion(REGION));
        assertThat(repository.objectExists(existsArgs(uploadedKey))).isFalse();

        repository.uploadObject(UploadLocalObjectArgs.builder()
                .bucket(BUCKET).key(uploadedKey).sourcePath(source.toString()).contentType(CONTENT_TYPE).build());
        assertThat(repository.objectExists(existsArgs(uploadedKey))).isTrue();

        var destination = tempDir.resolve("downloaded.txt");
        repository.downloadObject(DownloadLocalObjectArgs.builder()
                .bucket(BUCKET).key(uploadedKey).destinationPath(destination.toString()).build());
        assertThat(Files.readAllBytes(destination)).isEqualTo(content);

        try (var input = new ByteArrayInputStream(content)) {
            repository.putObject(PutLocalObjectArgs.builder()
                    .bucket(BUCKET).key(putKey).inputStream(input)
                    .contentLength(content.length).contentType(CONTENT_TYPE).build());
        }
        try (var input = repository.getObject(GetLocalObjectArgs.builder().bucket(BUCKET).key(putKey).build())) {
            assertThat(input.readAllBytes()).isEqualTo(content);
        }

        repository.deleteObject(DeleteLocalObjectArgs.builder().bucket(BUCKET).key(uploadedKey).build());
        repository.deleteObject(DeleteLocalObjectArgs.builder().bucket(BUCKET).key(putKey).build());
        assertThat(repository.objectExists(existsArgs(uploadedKey))).isFalse();
        assertThat(repository.objectExists(existsArgs(putKey))).isFalse();
    }

    private static LocalObjectExistsArgs existsArgs(String key) {
        return LocalObjectExistsArgs.builder().bucket(BUCKET).key(key).build();
    }

    private static String randomObjectKey() {
        return UUID.randomUUID().toString();
    }
}
