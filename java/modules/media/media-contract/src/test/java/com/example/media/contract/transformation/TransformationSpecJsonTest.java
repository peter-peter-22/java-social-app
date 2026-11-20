package com.example.media.contract.transformation;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The JSON shape of the specs is persisted in the registry and sent to workers.
 * These tests are the shape lock: the discriminators and property names must not drift.
 */
class TransformationSpecJsonTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void imageSpecRoundTripKeepsThePersistedShape() {
        TransformationSpec spec = new ImageTransformationSpec(
                new LimitResolution(100, LimitResolution.Mode.KEEP_ASPECT_RATIO),
                null,
                new AspectRatio(1, 1, AspectRatio.Mode.FILL),
                new ImageEncoding.Webp(50)
        );

        var json = objectMapper.writeValueAsString(spec);

        assertThat(json)
                .contains("\"type\":\"image\"")
                .contains("\"type\":\"webp\"")
                .contains("\"quality\":50")
                .contains("\"limitWidth\"")
                .contains("\"aspectRatio\"");
        assertThat(objectMapper.readValue(json, TransformationSpec.class)).isEqualTo(spec);
    }

    @Test
    void videoSpecUsesTheVideoDiscriminator() {
        TransformationSpec spec = new VideoTransformationSpec(new VideoTransformationSpec.VideoEncoding.Mp4());

        var json = objectMapper.writeValueAsString(spec);

        assertThat(json)
                .contains("\"type\":\"video\"")
                .contains("\"type\":\"mp4\"");
        assertThat(objectMapper.readValue(json, TransformationSpec.class)).isEqualTo(spec);
    }

    @Test
    void imageEncodingRoundTrip() {
        ImageEncoding encoding = new ImageEncoding.Jpeg(80);

        var json = objectMapper.writeValueAsString(encoding);

        assertThat(json).contains("\"type\":\"jpeg\"").contains("\"quality\":80");
        assertThat(objectMapper.readValue(json, ImageEncoding.class)).isEqualTo(encoding);
    }

    @Test
    void aMinimalSpecRoundTripsToo() {
        TransformationSpec spec = new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(100));

        var json = objectMapper.writeValueAsString(spec);

        assertThat(objectMapper.readValue(json, TransformationSpec.class)).isEqualTo(spec);
    }
}