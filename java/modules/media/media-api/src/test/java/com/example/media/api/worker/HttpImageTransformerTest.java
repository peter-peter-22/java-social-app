package com.example.media.api.worker;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.worker.CompletedImageOutput;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.contract.worker.PlannedImageOutput;
import com.example.media.json.MediaJson;
import org.junit.jupiter.api.Test;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * The blocking worker call travels over HTTP in the contract's JSON shape: the command is posted to
 * the worker's transform endpoint and the result is read from the response. The worker's failures
 * propagate to the caller.
 */
class HttpImageTransformerTest {

    private final ObjectMapper objectMapper = MediaJson.create();
    private final RestClient.Builder restClientBuilder = RestClient.builder()
            .baseUrl("http://worker.test")
            // The client speaks the shared media JSON shape, like the production adapter does.
            .configureMessageConverters(converters ->
                    converters.withJsonConverter(new JacksonJsonHttpMessageConverter(MediaJson.create())));
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
    private final RetryTemplate retryTemplate = new RetryTemplate(RetryPolicy.builder()
            .includes(ResourceAccessException.class, HttpServerErrorException.class)
            .maxRetries(2)
            .delay(Duration.ZERO)
            .build());
    private final HttpImageTransformer adapter =
            new HttpImageTransformer(restClientBuilder.build(), retryTemplate);

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final AssetPlacement PLACEMENT = new AssetPlacement("eu-west", "uploads");
    private static final AssetId OUTPUT_ID = AssetId.generate();
    private static final ImageTransformCommand COMMAND = new ImageTransformCommand(
            ATTEMPT_ID,
            new ObjectRef("eu-west", "uploads", "3f2b/original_jpg"),
            List.of(new PlannedImageOutput(
                    null,
                    PLACEMENT,
                    new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50)))));

    @Test
    void postsTheCommandAndReadsTheResult() {
        var result = new ImageTransformResult(
                ATTEMPT_ID,
                List.of(new CompletedImageOutput(
                        OUTPUT_ID,
                        PLACEMENT.prefixOf(OUTPUT_ID),
                        "default.webp",
                        List.of("default.webp"),
                        3,
                        "image/webp",
                        new ImageMetadata(100, 50))),
                new ImageMetadata(400, 300));
        server.expect(requestTo("http://worker.test/api/transform/image"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.attemptId.value").value(ATTEMPT_ID.value().toString()))
                .andExpect(jsonPath("$.outputs[0].placement.homeRegion").value("eu-west"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(result), MediaType.APPLICATION_JSON));

        assertThat(adapter.transform(COMMAND)).isEqualTo(result);
        server.verify();
    }

    @Test
    void retriesServerErrors() {
        server.expect(requestTo("http://worker.test/api/transform/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());
        server.expect(requestTo("http://worker.test/api/transform/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());
        var result = new ImageTransformResult(
                ATTEMPT_ID,
                List.of(new CompletedImageOutput(
                        OUTPUT_ID,
                        PLACEMENT.prefixOf(OUTPUT_ID),
                        "default.webp",
                        List.of("default.webp"),
                        3,
                        "image/webp",
                        new ImageMetadata(100, 50))),
                new ImageMetadata(400, 300));
        server.expect(requestTo("http://worker.test/api/transform/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(result), MediaType.APPLICATION_JSON));

        assertThat(adapter.transform(COMMAND)).isEqualTo(result);
        server.verify();
    }

    @Test
    void retriesTransportFailures() {
        server.expect(requestTo("http://worker.test/api/transform/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withException(new IOException("connection reset")));
        var result = new ImageTransformResult(
                ATTEMPT_ID,
                List.of(new CompletedImageOutput(
                        OUTPUT_ID,
                        PLACEMENT.prefixOf(OUTPUT_ID),
                        "default.webp",
                        List.of("default.webp"),
                        3,
                        "image/webp",
                        new ImageMetadata(100, 50))),
                new ImageMetadata(400, 300));
        server.expect(requestTo("http://worker.test/api/transform/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(result), MediaType.APPLICATION_JSON));

        assertThat(adapter.transform(COMMAND)).isEqualTo(result);
        server.verify();
    }

    @Test
    void doesNotRetryClientErrors() {
        server.expect(requestTo("http://worker.test/api/transform/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> adapter.transform(COMMAND))
                .isInstanceOf(HttpClientErrorException.class);
        server.verify();
    }

    @Test
    void exhaustedServerErrorsPropagateToTheCaller() {
        for (int i = 0; i <= 2; i++) {
            server.expect(requestTo("http://worker.test/api/transform/image"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withServerError());
        }

        assertThatThrownBy(() -> adapter.transform(COMMAND))
                .isInstanceOf(HttpServerErrorException.class);
        server.verify();
    }
}