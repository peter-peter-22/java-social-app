package com.example.image_transformer;

import com.example.image_transformer.storage.TestResourcesDirectory;
import com.example.object_storage.repository.ObjectStorageRepository;
import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.dto.ImageTransformationTaskSpecDTO;
import com.example.uploads_api.transformations.operations.LimitResolution;
import com.example.uploads_api.uploads.FileType;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(locations = "classpath:image-transformation-rest-test.properties")
class TransformationRestIT {
    private static final MockWebServer WEBHOOK_SERVER = startWebhookServer();

    @LocalServerPort
    private int serverPort;

    @Autowired
    private ObjectStorageRepository objectStorageRepository;

    @AfterAll
    static void stopWebhookServer() throws IOException {
        WEBHOOK_SERVER.close();
    }

    @DynamicPropertySource
    static void registerWebhookProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "transformations.webhook_url",
                () -> WEBHOOK_SERVER.url("/").toString()
        );
    }

    @Test
    void transformsImageThroughRestApiAndCallsWebhookForLazyTask() throws Exception {
        var input = objectLocation("input.jpg");
        var output = objectLocation("thumbnail.jpg");
        var uploadId = new UploadId(UUID.randomUUID());

        uploadInput(input);
        WEBHOOK_SERVER.enqueue(new MockResponse(200));

        var task = new ImageTransformationTaskSpecDTO(
                output,
                "thumbnail",
                true,
                new LimitResolution(400, LimitResolution.Mode.KEEP_ASPECT_RATIO),
                null,
                FileType.JPEG,
                85,
                null,
                uploadId
        );

        var response = restClient().post()
                .uri("/transform")
                .body(new ImageTransformationTaskGroupDTO(input, List.of(task)))
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertImageDimensions(output);

        var webhookRequest = WEBHOOK_SERVER.takeRequest();
        assertThat(webhookRequest.getMethod()).isEqualTo("POST");
        assertThat(webhookRequest.getPath()).isEqualTo("/");
        assertThat(webhookRequest.getBody().readUtf8())
                .contains(uploadId.get().toString())
                .contains("thumbnail");
    }

    @Test
    void doesNotCallWebhookForNonLazyTask() throws Exception {
        var input = objectLocation("input.jpg");
        var output = objectLocation("thumbnail.jpg");

        uploadInput(input);

        var task = new ImageTransformationTaskSpecDTO(
                output,
                "thumbnail",
                false,
                new LimitResolution(400, LimitResolution.Mode.KEEP_ASPECT_RATIO),
                null,
                FileType.JPEG,
                85,
                null,
                new UploadId(UUID.randomUUID())
        );

        var response = restClient().post()
                .uri("/transform")
                .body(new ImageTransformationTaskGroupDTO(input, List.of(task)))
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertImageDimensions(output);
        assertThat(WEBHOOK_SERVER.takeRequest(100, java.util.concurrent.TimeUnit.MILLISECONDS))
                .isNull();
    }

    private static InputStream getTestFileStream() {
        var inputPath = TestResourcesDirectory.getResourcesPath().resolve("test-images", "image.jpg");
        System.out.println("Reading test image from " + inputPath);
        try {
            return Files.newInputStream(inputPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read source image from " + inputPath, e);
        }
    }

    private void uploadInput(ObjectLocation location) throws IOException {
        try (InputStream input = getTestFileStream()) {
            assertThat(input).isNotNull();
            var bytes = input.readAllBytes();
            objectStorageRepository.putObject(
                    location,
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    "image/jpeg"
            );
        }
    }

    private void assertImageDimensions(ObjectLocation location) throws IOException {
        try (InputStream output = objectStorageRepository.getObject(location)) {
            var image = ImageIO.read(output);
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(400);
            assertThat(image.getHeight()).isEqualTo(266);
        }
    }

    private static ObjectLocation objectLocation(String name) {
        return new ObjectLocation("test/" + UUID.randomUUID() + "/" + name, "public");
    }

    private RestClient restClient() {
        return RestClient.create("http://localhost:" + serverPort);
    }

    private static MockWebServer startWebhookServer() {
        try {
            var server = new MockWebServer();
            server.start();
            return server;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
