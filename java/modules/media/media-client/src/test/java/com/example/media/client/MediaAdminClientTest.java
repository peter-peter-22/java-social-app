package com.example.media.client;

import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.upload.UploadVisibility;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
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
 * The admin facade speaks the admin endpoints over HTTP: the transformation definition is posted in
 * the shared media JSON shape with the admin key header, and the registered transformation is read
 * from the response. Error answers propagate to the caller.
 */
class MediaAdminClientTest {

    private static final ImageTransformationSpec WEBP_50 =
            new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50));

    private final RestClient.Builder restClientBuilder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
    private final MediaAdminClient client =
            new MediaAdminClient(restClientBuilder, "http://api.test", "test-key");

    @Test
    void registersATransformation() {
        server.expect(requestTo("http://api.test/api/admin/transformation"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Admin-Key", "test-key"))
                .andExpect(content().string(allOf(
                        containsString("\"name\":\"optimize\""),
                        containsString("\"type\":\"image\""),
                        containsString("\"type\":\"webp\""),
                        containsString("\"quality\":50"))))
                .andRespond(withSuccess(
                        "{\"name\":\"optimize\",\"spec\":{\"type\":\"image\",\"encoding\":{\"type\":\"webp\",\"quality\":50}}}",
                        MediaType.APPLICATION_JSON));

        var registered = client.registerTransformation("optimize", WEBP_50);

        assertThat(registered.name()).isEqualTo("optimize");
        assertThat(registered.spec()).isEqualTo(WEBP_50);
        server.verify();
    }

    @Test
    void createsAnUploadPermission() {
        server.expect(requestTo("http://api.test/api/admin/upload-permission"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Admin-Key", "test-key"))
                .andExpect(content().string(allOf(
                        containsString("\"uploadKey\":\"cover_123\""),
                        containsString("\"extension\":\"jpg\""),
                        containsString("\"visibility\":\"PRIVATE\""),
                        containsString("\"eagerTransformations\":[\"profile\",\"thumb\"]"),
                        containsString("\"expiresInSeconds\":600"),
                        containsString("\"maxUses\":1"))))
                .andRespond(withSuccess(
                        "{\"permissionId\":\"6c8c5f1e-5d0b-4f6a-9a3e-8b2f7d1a4c11\","
                                + "\"uploadKey\":\"cover_123\",\"permission\":\"up1_token\","
                                + "\"expiresAt\":\"2026-11-25T12:10:00Z\",\"maxUses\":1}",
                        MediaType.APPLICATION_JSON));

        var issued = client.createUploadPermission(new MediaAdminClient.NewUploadPermission(
                "cover_123", "jpg", "image/jpeg", 10485760L, List.of("profile", "thumb"), "optimize",
                UploadVisibility.PRIVATE, 600L, 1));

        assertThat(issued.uploadKey()).isEqualTo("cover_123");
        assertThat(issued.permission()).isEqualTo("up1_token");
        assertThat(issued.expiresAt()).isEqualTo(Instant.parse("2026-11-25T12:10:00Z"));
        assertThat(issued.maxUses()).isEqualTo(1);
        server.verify();
    }

    @Test
    void createsADownloadPermission() {
        server.expect(requestTo("http://api.test/api/admin/download-permission"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Admin-Key", "test-key"))
                .andExpect(content().string(allOf(
                        containsString("\"uploadKey\":\"cover_123\""),
                        containsString("\"expiresInSeconds\":900"))))
                .andRespond(withSuccess(
                        "{\"uploadKey\":\"cover_123\",\"version\":\"1790000000123\","
                                + "\"expiresAt\":\"2026-11-25T12:15:00Z\",\"expires\":1790000000,"
                                + "\"signature\":\"sig\",\"keyId\":\"k1\",\"pathPrefix\":\"/cdn/cover_123\"}",
                        MediaType.APPLICATION_JSON));

        var issued = client.createDownloadPermission(
                new MediaAdminClient.NewDownloadPermission("cover_123", 900L));

        assertThat(issued.uploadKey()).isEqualTo("cover_123");
        assertThat(issued.version()).isEqualTo("1790000000123");
        assertThat(issued.expires()).isEqualTo(1790000000L);
        assertThat(issued.signature()).isEqualTo("sig");
        assertThat(issued.keyId()).isEqualTo("k1");
        assertThat(issued.pathPrefix()).isEqualTo("/cdn/cover_123");
        server.verify();
    }

    @Test
    void anErrorAnswerIsThrownToTheCaller() {
        server.expect(requestTo("http://api.test/api/admin/transformation"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.registerTransformation("optimize", WEBP_50))
                .isInstanceOf(RestClientResponseException.class);
        server.verify();
    }
}
