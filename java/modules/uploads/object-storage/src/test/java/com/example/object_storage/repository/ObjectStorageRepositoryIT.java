package com.example.object_storage.repository;

import com.example.object_storage.MinioIntegrationTest;
import com.example.uploads_api.uploads.ObjectLocation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ObjectStorageRepositoryIT extends MinioIntegrationTest {
    private static final Path TEST_FILE_PATH = Path.of("src/test/resources/test.txt");
    private static final String BUCKET = "public";
    private static final String CONTENT_TYPE = "text/plain";
    private static final RestClient restClient = RestClient.create();

    @Autowired
    private ObjectStorageRepository objectStorageRepository;

    private String initObject() {
        String objectPath = UUID.randomUUID().toString();
        objectStorageRepository.uploadObject(
                TEST_FILE_PATH.toString(),
                new ObjectLocation(objectPath, BUCKET),
                CONTENT_TYPE
        );
        return objectPath;
    }

    @Test
    void testObjectExists() {
        var objectPath = initObject();
        assertThat(objectStorageRepository.objectExists(new ObjectLocation(objectPath, BUCKET))).isTrue();
        assertThat(objectStorageRepository.objectExists(new ObjectLocation("nothing", BUCKET))).isFalse();
    }

    // TODO: test expiration

    @Test
    void testGetPreSignedDownloadUrl() throws IOException {
        var objectPath = initObject();
        byte[] expectedContent = readTestContent();

        var url = objectStorageRepository.getPreSignedDownloadUrl(
                GetPreSignedDownloadUrlArgs.builder()
                        .location(new ObjectLocation(objectPath, BUCKET))
                        .expiresIn(300)
                        .build()
        );

        var response = restClient.get()
                .uri(URI.create(url))
                .retrieve()
                .toEntity(byte[].class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expectedContent);
    }

    @Test
    void testGetPreSignedUploadForm() throws IOException {
        var objectPath = UUID.randomUUID().toString();
        byte[] content = readTestContent();

        Map<String, String> formData = objectStorageRepository.getPreSignedUploadForm(
                GetPreSignedUploadFormArgs.builder()
                        .location(new ObjectLocation(objectPath, BUCKET))
                        .expiration(10)
                        .timeUnit(ChronoUnit.MINUTES)
                        .build()
        );

        assertThat(formData).containsKeys(
                "policy",
                "x-amz-algorithm",
                "x-amz-credential",
                "x-amz-date",
                "x-amz-signature"
        );

        var requestBody = buildUploadForm(formData, objectPath, content);
        var requestBuffer = new Buffer();
        requestBody.writeTo(requestBuffer);
        var response = restClient.post()
                .uri(URI.create(objectStorageRepository.getSignedUploadFormUrl(BUCKET)))
                .header("Content-Type", requestBody.contentType().toString())
                .body(requestBuffer.readByteArray())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isIn(200, 201, 204);
        assertStoredContent(objectPath, content);
    }

    /**
     * The signed upload form should fail when the form arguments don't match what was specified during signing.
     */
    @Test
    void testGetPreSignedUploadFormRejectsMismatchingObjectKey() throws IOException {
        var signedObjectPath = UUID.randomUUID().toString();
        var uploadedObjectPath = UUID.randomUUID().toString();
        byte[] content = readTestContent();

        Map<String, String> formData = objectStorageRepository.getPreSignedUploadForm(
                GetPreSignedUploadFormArgs.builder()
                        .location(new ObjectLocation(signedObjectPath, BUCKET))
                        .expiration(10)
                        .timeUnit(ChronoUnit.MINUTES)
                        .build()
        );

        var requestBody = buildUploadForm(formData, uploadedObjectPath, content);
        var requestBuffer = new Buffer();
        requestBody.writeTo(requestBuffer);

        try {
            restClient.post()
                    .uri(URI.create(objectStorageRepository.getSignedUploadFormUrl(BUCKET)))
                    .header("Content-Type", requestBody.contentType().toString())
                    .body(requestBuffer.readByteArray())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            assertThat(e.getStatusCode().is4xxClientError()).isTrue();
            assertThat(objectStorageRepository.objectExists(new ObjectLocation(signedObjectPath, BUCKET))).isFalse();
            assertThat(objectStorageRepository.objectExists(new ObjectLocation(uploadedObjectPath, BUCKET))).isFalse();
            return;
        }

        throw new AssertionError("Expected mismatching object key upload to fail");
    }

    @Test
    void testPutObject() throws IOException {
        var objectPath = UUID.randomUUID().toString();
        byte[] content = readTestContent();

        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            objectStorageRepository.putObject(new ObjectLocation(objectPath, BUCKET), inputStream, content.length, CONTENT_TYPE);
        }

        assertStoredContent(objectPath, content);
    }

    @Test
    void testDownloadObject() throws IOException {
        var objectPath = initObject();
        byte[] expectedContent = readTestContent();

        byte[] responseBody = restClient.get()
                .uri(URI.create(objectStorageRepository.getDownloadUrl(new ObjectLocation(objectPath, BUCKET))))
                .retrieve()
                .body(byte[].class);

        assertThat(responseBody).isEqualTo(expectedContent);
    }

    @Test
    void testDeleteObject() {
        var objectPath = initObject();
        assertThat(objectStorageRepository.objectExists(new ObjectLocation(objectPath, BUCKET))).isTrue();

        objectStorageRepository.deleteObject(new ObjectLocation(objectPath, BUCKET));

        assertThat(objectStorageRepository.objectExists(new ObjectLocation(objectPath, BUCKET))).isFalse();
    }

    private static byte @NonNull [] readTestContent() throws IOException {
        return Files.readAllBytes(TEST_FILE_PATH);
    }

    private void assertStoredContent(String objectPath, byte[] expectedContent) throws IOException {
        assertThat(objectStorageRepository.objectExists(new ObjectLocation(objectPath, BUCKET))).isTrue();
        try (InputStream inputStream = objectStorageRepository.getObject(new ObjectLocation(objectPath, BUCKET))) {
            assertThat(inputStream.readAllBytes()).isEqualTo(expectedContent);
        }
    }

    private static @NonNull MultipartBody buildUploadForm(@NonNull Map<String, String> formData, String objectPath, byte[] content) {
        var builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        formData.forEach(builder::addFormDataPart);
        builder.addFormDataPart("bucket", BUCKET);
        builder.addFormDataPart("key", objectPath);
        builder.addFormDataPart(
                "file",
                objectPath,
                RequestBody.create(content, MediaType.parse(CONTENT_TYPE))
        );
        return builder.build();
    }
}
