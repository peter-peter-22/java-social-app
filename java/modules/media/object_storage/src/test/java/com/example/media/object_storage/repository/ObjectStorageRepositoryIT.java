package com.example.media.object_storage.repository;

import com.example.media.object_storage.MinioIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ObjectStorageRepositoryIT extends MinioIntegrationTest {
    @Autowired
    private ObjectStorageRepository objectStorageRepository;

    private static final String filePath = "src/test/resources/test.txt";
    private static final String bucket = "public";
    private static final String contentType = "text/plain";

    private static final RestClient restClient = RestClient.create();

    private record MultipartBody(byte[] bytes, String boundary) {}

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
    void testObjectExists() {
        var objectPath = initObject();
        assertThat(objectStorageRepository.objectExists(bucket, objectPath)).isTrue();
        assertThat(objectStorageRepository.objectExists(bucket, "nothing")).isFalse();
    }

    @Test
    void testGetPreSignedDownloadUrl() throws IOException {
        var objectPath = initObject();
        Path path = Paths.get(filePath);
        byte[] expectedContent = Files.readAllBytes(path);

        var url = objectStorageRepository.getPreSignedDownloadUrl(
                GetPreSignedDownloadUrlArgs.builder()
                        .bucket(bucket)
                        .objectPath(objectPath)
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
        byte[] content = Files.readAllBytes(Paths.get(filePath));

        Map<String, String> formData = objectStorageRepository.getPreSignedUploadForm(
                GetPreSignedUploadFormArgs.builder()
                        .bucket(bucket)
                        .objectPath(objectPath)
                        .expiration(10)
                        .timeUnit(ChronoUnit.MINUTES)
                        .build()
        );

        String policyJson = new String(Base64.getDecoder().decode(formData.get("policy")), StandardCharsets.UTF_8);
        assertThat(policyJson).contains(bucket, objectPath);

        Map<String, String> uploadFormData = new LinkedHashMap<>(formData);
        uploadFormData.put("bucket", bucket);
        uploadFormData.put("key", objectPath);

        var requestBody = buildMultipartBody(uploadFormData, "file", objectPath, contentType, content);
        String uploadUrl = objectStorageRepository.getBucketUrl(bucket);

        var response = restClient.post()
                .uri(URI.create(uploadUrl))
                .header("Content-Type", "multipart/form-data; boundary=" + requestBody.boundary())
                .body(requestBody.bytes())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isIn(200, 201, 204);
        assertThat(objectStorageRepository.objectExists(bucket, objectPath)).isTrue();

        try (InputStream inputStream = objectStorageRepository.getObject(bucket, objectPath)) {
            assertThat(inputStream.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    void testPutObject() throws IOException {
        var objectPath = UUID.randomUUID().toString();
        byte[] content = Files.readAllBytes(Paths.get(filePath));

        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            objectStorageRepository.putObject(bucket, objectPath, inputStream, content.length, contentType);
        }

        assertThat(objectStorageRepository.objectExists(bucket, objectPath)).isTrue();

        try (InputStream inputStream = objectStorageRepository.getObject(bucket, objectPath)) {
            assertThat(inputStream.readAllBytes()).isEqualTo(content);
        }
    }

    /**
     * The downloaded content should match the original content
     */
    @Test
    void testDownloadObject() throws IOException {
        var objectPath = initObject();

        Path path = Paths.get(filePath);
        byte[] expectedContent = Files.readAllBytes(path);

        byte[] responseBody = restClient.get()
                .uri(URI.create(objectStorageRepository.getDownloadUrl(bucket, objectPath)))
                .retrieve()
                .body(byte[].class);

        assertThat(responseBody).isEqualTo(expectedContent);
    }

    @Test
    void testDeleteObject() {
        var objectPath = initObject(); // Create and upload an object
        assertThat(objectStorageRepository.objectExists(bucket, objectPath)).isTrue();

        objectStorageRepository.deleteObject(bucket, objectPath); // Delete the object

        assertThat(objectStorageRepository.objectExists(bucket, objectPath)).isFalse(); // Verify deletion
    }

    private static MultipartBody buildMultipartBody(
            Map<String, String> formData,
            String fileFieldName,
            String fileName,
            String fileContentType,
            byte[] fileContent
    ) throws IOException {
        String boundary = "----ObjectStorageRepositoryIT" + UUID.randomUUID().toString().replace("-", "");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (var entry : new LinkedHashMap<>(formData).entrySet()) {
            if ("url".equals(entry.getKey())) {
                continue;
            }
            writeLine(outputStream, "--" + boundary);
            writeLine(outputStream, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"");
            writeLine(outputStream, "");
            writeLine(outputStream, entry.getValue());
        }

        writeLine(outputStream, "--" + boundary);
        writeLine(outputStream, "Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + fileName + "\"");
        writeLine(outputStream, "Content-Type: " + fileContentType);
        writeLine(outputStream, "");
        outputStream.write(fileContent);
        writeLine(outputStream, "");
        writeLine(outputStream, "--" + boundary + "--");

        return new MultipartBody(outputStream.toByteArray(), boundary);
    }

    private static void writeLine(ByteArrayOutputStream outputStream, String value) throws IOException {
        outputStream.write(value.getBytes(StandardCharsets.UTF_8));
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
