package com.example.media.contract.worker;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.object.ObjectPrefix;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.upload.AttemptId;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The worker messages travel over JSON between the API and the workers.
 * These tests are the wire-shape lock.
 */
class WorkerMessageJsonTest {

    ObjectMapper objectMapper = new ObjectMapper();

    private final AttemptId attemptId = new AttemptId(UUID.randomUUID());

    @Test
    void commandRoundTrip() {
        var command = new ImageTransformCommand(
                attemptId,
                new ObjectRef("eu-west", "uploads", "3f2b/original_jpg"),
                List.of(
                        new PlannedImageOutput(
                                AssetId.generate(),
                                new ObjectPrefix("eu-west", "uploads", "a1"),
                                null,
                                new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50))
                        ),
                        new PlannedImageOutput(
                                AssetId.generate(),
                                new ObjectPrefix("eu-west", "uploads", "b2"),
                                "profile",
                                new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(100))
                        )
                )
        );

        var json = objectMapper.writeValueAsString(command);

        assertThat(json).contains("\"transformationName\":\"profile\"");
        assertThat(objectMapper.readValue(json, ImageTransformCommand.class)).isEqualTo(command);
    }

    @Test
    void resultRoundTrip() {
        var result = new ImageTransformResult(
                attemptId,
                List.of(new CompletedImageOutput(
                        AssetId.generate(),
                        new ObjectPrefix("eu-west", "uploads", "a1"),
                        "original_webp",
                        List.of("original_webp"),
                        1234,
                        "image/webp",
                        new ImageMetadata(100, 50)
                )),
                new ImageMetadata(400, 300)
        );

        var json = objectMapper.writeValueAsString(result);

        assertThat(objectMapper.readValue(json, ImageTransformResult.class)).isEqualTo(result);
    }

    @Test
    void resultWithoutSourceMetadataRoundTrips() {
        var result = new ImageTransformResult(attemptId, List.of(), null);

        var json = objectMapper.writeValueAsString(result);

        assertThat(objectMapper.readValue(json, ImageTransformResult.class)).isEqualTo(result);
    }
}