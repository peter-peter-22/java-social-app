package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;

import static com.example.uploads_api.utils.TestTransformationCreator.*;

import static com.example.uploads_api.utils.TestUploadCreator.*;

import com.example.uploads_api.transformations.dto.VideoTransformationTaskGroupDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockingImageTransformerRestApiTests {
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

            var tasks = new ImageTransformationTaskGroupDTO(
                    List.of(createImageTransformation().createTaskDTO(createImage()))
            );

            api.transformAll(tasks);

            var request = server.takeRequest();
            var requestBody = OBJECT_MAPPER.readTree(request.getBody().readUtf8());
            var expectedBody = OBJECT_MAPPER.valueToTree(tasks);

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

            var tasks = new VideoTransformationTaskGroupDTO(
                    List.of(createVideoTransformation().createTaskDTO(createVideo()))
            );

            api.transformAll(tasks);

            var request = server.takeRequest();
            var requestBody = OBJECT_MAPPER.readTree(request.getBody().readUtf8());
            var expectedBody = OBJECT_MAPPER.valueToTree(tasks);

            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/transform");
            assertThat(request.getHeaders().get("Content-Type")).startsWith("application/json");
            assertThat(requestBody).isEqualTo(expectedBody);
        }
    }
}
