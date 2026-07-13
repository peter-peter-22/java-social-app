package com.example.uploads.transformations;

import com.example.media_api.transformations.operations.AspectRatio;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.LimitResolution;
import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.UploadId;
import com.fasterxml.jackson.databind.ObjectMapper;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageTransformerRestApiTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private TransformationProperties properties;

    @Test
    void callPostsTransformationPayloadToTransformerApi() throws Exception {
        try (var server = new MockWebServer()) {
            server.enqueue(new MockResponse(200));
            server.start();

            when(properties.imageTransformerUrl()).thenReturn(URI.create(server.url("/").toString()));
            var api = new ImageTransformerRestApi(properties);

            var transformation = transformation();

            api.call(transformation);

            var request = server.takeRequest();
            var requestBody = OBJECT_MAPPER.readTree(request.getBody().readUtf8());
            var expectedBody = OBJECT_MAPPER.valueToTree(transformation);

            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/transform");
            assertThat(request.getHeaders().get("Content-Type")).startsWith("application/json");
            assertThat(requestBody).isEqualTo(expectedBody);
        }
    }

    private UploadTransformationTask transformation() {
        return UploadTransformationTask.builder()
                .name("transformation")
                .original(new UploadId(UUID.randomUUID()))
                .outputBucket("transformations")
                .operations(
                        ImageTransformationOperations.builder()
                                .limitWidth(new LimitResolution(640, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .format(FileType.JPEG)
                                .quality(85)
                                .aspectRatio(new AspectRatio(4, 3, AspectRatio.Mode.CONTAIN))
                                .build()
                )
                .build();
    }
}
