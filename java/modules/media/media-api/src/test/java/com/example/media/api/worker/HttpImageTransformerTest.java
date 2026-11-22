package com.example.media.api.worker;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.object.ObjectPrefix;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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
    private final HttpImageTransformer adapter = new HttpImageTransformer(restClientBuilder.build());

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final ImageTransformCommand COMMAND = new ImageTransformCommand(
            ATTEMPT_ID,
            new ObjectRef("eu-west", "uploads", "3f2b/original_jpg"),
            List.of(new PlannedImageOutput(
                    AssetId.generate(),
                    new ObjectPrefix("eu-west", "uploads", "a1"),
                    null,
                    new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50)))));

    @Test
    void postsTheCommandAndReadsTheResult() {
        var result = new ImageTransformResult(
                ATTEMPT_ID,
                List.of(new CompletedImageOutput(
                        COMMAND.outputs().getFirst().assetId(),
                        COMMAND.outputs().getFirst().prefix(),
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
                .andExpect(jsonPath("$.outputs[0].prefix.keyPrefix").value("a1"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(result), MediaType.APPLICATION_JSON));

        assertThat(adapter.transform(COMMAND)).isEqualTo(result);
        server.verify();
    }

    @Test
    void aWorkerFailurePropagatesToTheCaller() {
        server.expect(requestTo("http://worker.test/api/transform/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.transform(COMMAND)).isInstanceOf(RestClientException.class);
    }
}