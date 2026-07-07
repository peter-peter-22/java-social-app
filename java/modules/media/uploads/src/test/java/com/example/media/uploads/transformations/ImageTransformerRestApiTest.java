package com.example.media.uploads.transformations;

import com.example.media_api.transformations.api.MediaTransformerEndpoints;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.AspectRatio;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.LimitResolution;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.UploadId;
import com.fasterxml.jackson.databind.ObjectMapper;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageTransformerRestApiTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void callPostsTransformationPayloadToTransformerApi() throws Exception {
        try (var server = new MockWebServer()) {
            server.enqueue(new MockResponse(200));
            server.start();

            try (var context = createContext(server)) {
                var api = context.getBean(ImageTransformerRestApi.class);
                var transformation = transformation();

                api.call(transformation);

                var request = server.takeRequest();
                var requestBody = OBJECT_MAPPER.readTree(request.getBody().readUtf8());
                var expectedBody = OBJECT_MAPPER.valueToTree(transformation);

                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getPath()).isEqualTo(MediaTransformerEndpoints.TRANSFORM);
                assertThat(request.getHeaders().get("Content-Type")).startsWith("application/json");
                assertThat(requestBody).isEqualTo(expectedBody);
            }
        }
    }

    @Test
    void callRetriesForInternalServerError() throws Exception {
        try (var server = new MockWebServer()) {
            server.enqueue(new MockResponse(500));
            server.enqueue(new MockResponse(500));
            server.enqueue(new MockResponse(500));
            server.enqueue(new MockResponse(200));
            server.start();

            try (var context = createContext(server)) {
                var api = context.getBean(ImageTransformerRestApi.class);

                assertDoesNotThrow(() -> api.call(transformation()));
                assertThat(server.getRequestCount()).isEqualTo(4);
            }
        }
    }

    @Test
    void callDoesNotRetryForBadRequest() throws Exception {
        try (var server = new MockWebServer()) {
            server.enqueue(new MockResponse(400));
            server.start();

            try (var context = createContext(server)) {
                var api = context.getBean(ImageTransformerRestApi.class);

                assertThrows(HttpClientErrorException.BadRequest.class, () -> api.call(transformation()));
                assertThat(server.getRequestCount()).isEqualTo(1);
            }
        }
    }

    private AnnotationConfigApplicationContext createContext(MockWebServer server) {
        var context = new AnnotationConfigApplicationContext();
        var imageTransformerUrl = URI.create(server.url("/").toString());

        context.register(TestConfig.class);
        context.registerBean(TransformationProperties.class,
                () -> new TransformationProperties(imageTransformerUrl, URI.create("http://unused.local")));
        context.refresh();

        return context;
    }

    private UploadTransformationDTO transformation() {
        return new UploadTransformationDTO(
                "preview",
                "transformed",
                new UploadId("image.jpg", "uploads"),
                "https://example.test/webhook",
                ImageTransformationOperations.builder()
                        .limitWidth(new LimitResolution(640, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                        .format(FileType.JPEG)
                        .quality(85)
                        .aspectRatio(new AspectRatio(4, 3, AspectRatio.Mode.CONTAIN))
                        .build()
        );
    }

    @Configuration
    @EnableResilientMethods(proxyTargetClass = true)
    static class TestConfig {
        @Bean
        ImageTransformerRestApi imageTransformerRestApi(TransformationProperties properties) {
            return new ImageTransformerRestApi(properties);
        }
    }
}
