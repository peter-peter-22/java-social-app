package com.example.object_storage;

import com.example.object_storage.repository.ObjectStorageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ObjectStorageRepositoryIT extends MinioIntegrationTest {
    @Autowired
    private ObjectStorageRepository objectStorageRepository;

    private static final String filePath = "src/test/resources/test.txt";
    private static final String bucket = "public";
    private static final String contentType = "text/plain";

    private String initObject() {
        String objectPath = UUID.randomUUID().toString();
        objectStorageRepository.uploadObject(
                filePath,
                objectPath,
                bucket,
                contentType
        );
        return objectPath;
    }

    /**
     * The existing and non-existing objects should return the correct value
     */
    @Test
    void should_check_existence() {
        var objectPath = initObject();
        assertThat(objectStorageRepository.objectExists(bucket, objectPath)).isTrue();
        assertThat(objectStorageRepository.objectExists(bucket, "nothing")).isFalse();
    }

    /**
     * The downloaded content should match the original content
     */
    @Test
    void should_download_object() throws IOException, InterruptedException {
        var objectPath = initObject();
        Path path = Paths.get(filePath);
        byte[] expectedContent = Files.readAllBytes(path);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder(URI.create(objectStorageRepository.getDownloadUrl(bucket, objectPath)))
                        .GET()
                        .build();

        HttpResponse<byte[]> response =
                client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo(expectedContent);
    }
}
