package com.example.media.api;

import com.example.media.client.MediaAdminClient;
import com.example.media.client.MediaClient;
import com.example.media.contract.transformation.AspectRatio;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.upload.UploadAttemptStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The behavior lock of the media API. It boots the API in-process on a real port and drives the
 * complete stack through the Java client only — no in-process ports, no registry internals: the
 * {@link MediaComposeStack} starts the worker, the database, the object storage, and RabbitMQ from
 * the deployment compose file. The journeys: register {@code optimize} and {@code profile} as the
 * admin, upload an image with an incoming and an eager transformation and read the results back
 * (decoding the variant to lock its dimensions), the same upload over the queue finalized by the
 * worker, and a corrupt source marked failed. One command runs it:
 * {@code ./gradlew :media-api:integrationTest}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
        "minio.reconciliation.enabled=true",
        "media.admin-key=e2e-admin-key"
})
public class EndToEndTest {

    static {
        // The stack must be up before the Spring context boots and runs the migrations.
        MediaComposeStack.start();
    }

    private static final String ADMIN_KEY = "e2e-admin-key";

    @LocalServerPort
    int port;

    MediaClient client;

    MediaAdminClient adminClient;

    @BeforeEach
    void createClients() {
        var url = "http://localhost:" + port;
        client = new MediaClient(RestClient.builder(), url);
        adminClient = new MediaAdminClient(RestClient.builder(), url, ADMIN_KEY);
    }

    @Test
    void contextLoads() {
    }

    /** The eager and incoming transformations run blocking, so the upload is readable when the response arrives. */
    @Test
    void blockingUploadThenRead() throws Exception {
        registerTransformations();
        var uploadKey = "test" + UUID.randomUUID();

        var result = client.uploadImage(request(uploadKey, false), imageBytes(), "image.jpg", "image/jpeg");

        assertThat(result.uploadKey()).isEqualTo(uploadKey);
        assertThat(result.async()).isFalse();
        assertThat(result.createdAt()).isNotNull();

        assertReadable(uploadKey);
    }

    /** The same upload over the queue: the API accepts it immediately and the worker finalizes it. */
    @Test
    void asyncUploadIsFinalizedByTheWorker() throws Exception {
        registerTransformations();
        var uploadKey = "test" + UUID.randomUUID();

        var result = client.uploadImage(request(uploadKey, true), imageBytes(), "image.jpg", "image/jpeg");

        assertThat(result.uploadKey()).isEqualTo(uploadKey);
        assertThat(result.async()).isTrue();

        awaitUntil(() -> status(uploadKey) == UploadAttemptStatus.SUCCESS, Duration.ofSeconds(60),
                () -> "the async attempt of " + uploadKey + " ended in " + status(uploadKey));

        assertReadable(uploadKey);
    }

    /** The failure lock: a non-image source fails in the worker and the attempt is marked failed. */
    @Test
    void aCorruptSourceMarksTheAsyncAttemptFailed() {
        registerTransformations();
        var uploadKey = "test" + UUID.randomUUID();

        var result = client.uploadImage(request(uploadKey, true), new byte[]{1, 2, 3}, "image.jpg", "text/plain");

        assertThat(result.async()).isTrue();

        awaitUntil(() -> status(uploadKey) == UploadAttemptStatus.FAILED, Duration.ofSeconds(90),
                () -> "the corrupt async attempt of " + uploadKey + " ended in " + status(uploadKey));
    }

    /** The original was replaced by the incoming webp transformation and the profile variant is a square. */
    private void assertReadable(String uploadKey) throws IOException {
        var original = client.readOriginal(uploadKey, "jpg");
        assertThat(original.contentType()).isEqualTo("image/webp");
        assertThat(original.content()).isNotEmpty();

        var variant = client.readVariant(uploadKey, "profile", "jpg");
        assertThat(variant.contentType()).isEqualTo("image/jpeg");
        var decoded = ImageIO.read(new ByteArrayInputStream(variant.content()));
        assertThat(decoded).isNotNull();
        // the profile transformation fills a 1:1 aspect ratio: the variant must be square
        assertThat(decoded.getWidth()).isEqualTo(decoded.getHeight());
    }

    private MediaClient.UploadImageRequest request(String uploadKey, boolean async) {
        return new MediaClient.UploadImageRequest(
                uploadKey, "jpg", null, null, List.of("profile"), "optimize", async);
    }

    private void registerTransformations() {
        adminClient.registerTransformation("optimize",
                new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50)));
        adminClient.registerTransformation("profile",
                new ImageTransformationSpec(
                        null,
                        null,
                        new AspectRatio(1, 1, AspectRatio.Mode.FILL),
                        new ImageEncoding.Jpeg(80)));
    }

    private byte[] imageBytes() throws IOException {
        return new ClassPathResource("image.jpg").getContentAsByteArray();
    }

    /** The status of the key's active attempt; null while the attempt is not visible yet. */
    private UploadAttemptStatus status(String uploadKey) {
        try {
            return client.uploadStatus(uploadKey).status();
        } catch (RuntimeException e) {
            return null;
        }
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
}
