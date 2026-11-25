package com.example.media.client;

import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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
    void anErrorAnswerIsThrownToTheCaller() {
        server.expect(requestTo("http://api.test/api/admin/transformation"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.registerTransformation("optimize", WEBP_50))
                .isInstanceOf(RestClientResponseException.class);
        server.verify();
    }
}
