package com.example.media.api;

import com.example.media.api.web.ImageUploadRequest;
import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.transformation.AspectRatio;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.transformation.NewNamedTransformation;
import com.example.media.contract.upload.UploadAttemptStatus;
import com.example.media.contract.upload.UploadKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The behavior lock of the media API. To run it the image worker, the database, the object storage,
 * and RabbitMQ must be running (see the {@code deployment} folder). It boots the API in-process on
 * a real port and drives it over HTTP: register {@code optimize} and {@code profile}, upload an
 * image with an incoming and an eager transformation, then read the original as WebP and the named
 * variant. The same upload over the queue is finalized asynchronously by the worker.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {"minio.reconciliation.enabled=true"})
public class EndToEndTest {

    @LocalServerPort
    int port;

    RestClient restClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TransformationCatalog transformationCatalog;

    @Autowired
    UploadRegistry uploadRegistry;

    @BeforeEach
    void keepErrorResponsesForAssertions() {
        // The expected 4xx answers are asserted on the response, not thrown as exceptions.
        restClient = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(HttpClient.newBuilder().build()))
                .defaultStatusHandler(status -> true, (request, response) -> {
                })
                .build();
    }

    @Test
    void contextLoads() {
    }

    /** The eager and incoming transformations run blocking, so the upload is readable when the response arrives. */
    @Test
    void blockingUploadThenRead() throws Exception {
        registerTransformations();
        var uploadKey = "test" + UUID.randomUUID();

        var response = upload(uploadKey, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        var json = objectMapper.readTree(response.getBody());
        assertThat(json.get("uploadKey").asText()).isEqualTo(uploadKey);
        assertThat(json.get("async").asBoolean()).isFalse();

        readOriginal(uploadKey);
        readVariant(uploadKey);
    }

    /** The same upload over the queue: the API accepts it immediately and the worker finalizes it. */
    @Test
    void asyncUploadIsFinalizedByTheWorker() throws Exception {
        registerTransformations();
        var uploadKey = "test" + UUID.randomUUID();

        var response = upload(uploadKey, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        var json = objectMapper.readTree(response.getBody());
        assertThat(json.get("uploadKey").asText()).isEqualTo(uploadKey);
        assertThat(json.get("async").asBoolean()).isTrue();

        awaitUntil(() -> attemptStatus(uploadKey) == UploadAttemptStatus.SUCCESS, Duration.ofSeconds(60),
                () -> "the async attempt of " + uploadKey + " ended in " + attemptStatus(uploadKey));

        readOriginal(uploadKey);
        readVariant(uploadKey);
    }

    /** The failure lock: a non-image source fails in the worker and the attempt is marked failed. */
    @Test
    void aCorruptSourceMarksTheAsyncAttemptFailed() {
        registerTransformations();
        var uploadKey = "test" + UUID.randomUUID();

        var response = upload(uploadKey, true, "text/plain", new byte[]{1, 2, 3});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        awaitUntil(() -> attemptStatus(uploadKey) == UploadAttemptStatus.FAILED, Duration.ofSeconds(90),
                () -> "the corrupt async attempt of " + uploadKey + " ended in " + attemptStatus(uploadKey));
    }

    private UploadAttemptStatus attemptStatus(String uploadKey) {
        var process = uploadRegistry.openOrGetProcess(new UploadKey(uploadKey));
        if (process.activeAttemptId() == null)
            return null;
        return uploadRegistry.findAttempt(process.activeAttemptId())
                .map(attempt -> attempt.status())
                .orElse(null);
    }

    private void registerTransformations() {
        transformationCatalog.register(new NewNamedTransformation(
                "optimize",
                new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50))));
        transformationCatalog.register(new NewNamedTransformation(
                "profile",
                new ImageTransformationSpec(
                        null,
                        null,
                        new AspectRatio(1, 1, AspectRatio.Mode.FILL),
                        new ImageEncoding.Webp(80))));
    }

    private ResponseEntity<String> upload(String uploadKey, boolean async) throws Exception {
        return upload(uploadKey, async, MediaType.IMAGE_JPEG_VALUE,
                new ClassPathResource("image.jpg").getContentAsByteArray());
    }

    private ResponseEntity<String> upload(String uploadKey, boolean async, String contentType, byte[] file) {
        var params = new ImageUploadRequest(
                uploadKey, "jpg", null, null, List.of("profile"), "optimize", async);

        var jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        var fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(contentType));
        var parts = new LinkedMultiValueMap<String, Object>();
        parts.add("body", new HttpEntity<>(objectMapper.writeValueAsBytes(params), jsonHeaders));
        parts.add("file", new HttpEntity<>(new ByteArrayResourceWithFilename(file, "image.jpg"), fileHeaders));

        return restClient.post()
                .uri(url("/api/upload/image"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .toEntity(String.class);
    }

    private void readOriginal(String uploadKey) {
        var response = restClient.get()
                .uri(url("/api/get/image/%s.jpg".formatted(uploadKey)))
                .retrieve()
                .toEntity(byte[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("image/webp"));
    }

    private void readVariant(String uploadKey) {
        var response = restClient.get()
                .uri(url("/api/get/image/%s/profile.webp".formatted(uploadKey)))
                .retrieve()
                .toEntity(byte[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("image/webp"));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static void awaitUntil(BooleanSupplier condition, Duration timeout, Supplier<String> message) {
        var deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            if (condition.getAsBoolean())
                return;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted while waiting", e);
            }
        }
        throw new AssertionError(message.get());
    }

    /** A byte array that carries a file name for the multipart part. */
    private static final class ByteArrayResourceWithFilename extends ByteArrayResource {
        private final String filename;

        ByteArrayResourceWithFilename(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }

}
