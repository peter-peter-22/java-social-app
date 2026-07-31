package com.example.uploads_service.transformation_service;

import com.example.uploads_api.utils.TestTransformationTaskGroupCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockingTransformerRestApiTests {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private TransformationProperties properties;

    @Test
    void testImageApi() throws Exception {
        try (var server = new MockWebServer()) {
            server.enqueue(new MockResponse(200));
            server.start();

            when(properties.imageTransformerUrl()).thenReturn(URI.create(server.url("/").toString()));
            var api = new BlockingImageTransformerRestApi(properties);

            var tasks = TestTransformationTaskGroupCreator.createImageTransformationTaskGroup();

            api.transformAll(tasks);

            var request = server.takeRequest();
            var requestBody = OBJECT_MAPPER.readTree(request.getBody().readUtf8());
            var expectedBody = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(tasks));

            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/transform");
            assertThat(request.getHeaders().get("Content-Type")).startsWith("application/json");
            assertThat(requestBody).isEqualTo(expectedBody);
        }
    }

    @Test
    void testVideoApi() throws Exception {
        try (var server = new MockWebServer()) {
            server.enqueue(new MockResponse(200));
            server.start();

            when(properties.videoTransformerUrl()).thenReturn(URI.create(server.url("/").toString()));
            var api = new BlockingVideoTransformerRestApi(properties);

            var tasks = TestTransformationTaskGroupCreator.createVideoTransformationTaskGroup();

            api.transformAll(tasks);

            var request = server.takeRequest();
            var requestBody = OBJECT_MAPPER.readTree(request.getBody().readUtf8());
            var expectedBody = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(tasks));

            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/transform");
            assertThat(request.getHeaders().get("Content-Type")).startsWith("application/json");
            assertThat(requestBody).isEqualTo(expectedBody);
        }
    }
}
