package com.example.media.client;

import com.example.media.contract.upload.UploadAttemptStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * The web facade speaks the client endpoints over HTTP: the upload is a multipart request with a
 * JSON body part and a file part, the reads stream stored files back, and the status endpoint
 * reports the attempt state. Error answers propagate to the caller.
 */
class MediaClientTest {

    private final RestClient.Builder restClientBuilder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
    private final MediaClient client = new MediaClient(restClientBuilder, "http://api.test");

    @Test
    void uploadsAnImage() {
        server.expect(requestTo("http://api.test/api/upload/image"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(allOf(
                        containsString("name=\"body\""),
                        containsString("\"permission\":\"up1_token\""),
                        containsString("\"uploadKey\":\"cover\""),
                        containsString("\"async\":true"),
                        containsString("name=\"file\""),
                        containsString("filename=\"image.jpg\""))))
                .andRespond(withSuccess(
                        "{\"uploadKey\":\"cover\",\"createdAt\":\"2026-01-01T00:00:00Z\",\"async\":true}",
                        MediaType.APPLICATION_JSON));

        var result = client.uploadImage(
                new MediaClient.UploadImageRequest(
                        "up1_token", "cover", "jpg", null, null, List.of("profile"), "optimize", true),
                new byte[]{1, 2, 3}, "image.jpg", "image/jpeg");

        assertThat(result.uploadKey()).isEqualTo("cover");
        assertThat(result.createdAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(result.async()).isTrue();
        server.verify();
    }

    @Test
    void readsTheOriginal() {
        server.expect(requestTo("http://api.test/api/get/image/cover.jpg"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(new byte[]{9, 9}, MediaType.parseMediaType("image/webp"))
                        .header("Content-Disposition", "inline; filename=\"cover.jpg\""));

        var image = client.readOriginal("cover", "jpg");

        assertThat(image.contentType()).isEqualTo("image/webp");
        assertThat(image.fileName()).isEqualTo("cover.jpg");
        assertThat(image.content()).isEqualTo(new byte[]{9, 9});
        server.verify();
    }

    @Test
    void readsTheVariant() {
        server.expect(requestTo("http://api.test/api/get/image/cover/profile.webp"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(new byte[]{7}, MediaType.parseMediaType("image/webp")));

        var image = client.readVariant("cover", "profile", "webp");

        assertThat(image.contentType()).isEqualTo("image/webp");
        assertThat(image.content()).isEqualTo(new byte[]{7});
        server.verify();
    }

    @Test
    void readsTheUploadStatus() {
        server.expect(requestTo("http://api.test/api/upload/status/cover"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"uploadKey\":\"cover\",\"status\":\"PENDING\"}", MediaType.APPLICATION_JSON));

        var status = client.uploadStatus("cover");

        assertThat(status.uploadKey()).isEqualTo("cover");
        assertThat(status.status()).isEqualTo(UploadAttemptStatus.PENDING);
        server.verify();
    }

    @Test
    void aMissingUploadThrows() {
        server.expect(requestTo("http://api.test/api/get/image/missing.jpg"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.readOriginal("missing", "jpg"))
                .isInstanceOf(RestClientResponseException.class);
        server.verify();
    }
}
