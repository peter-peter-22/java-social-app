package com.example.media.registry;

import com.example.media.contract.transformation.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The registry persists transformation specs in the jsonb column through the
 * {@link MediaJsonFormatMapper}, which speaks the shared {@code media-json} shape — the same shape
 * the wire format uses. These tests check the bridge round-trips; the JSON shape itself is locked
 * by the tests in {@code media-json}.
 */
class MediaJsonFormatMapperTest {

    private final MediaJsonFormatMapper formatMapper = new MediaJsonFormatMapper();

    @Test
    void imageSpecRoundTripsThroughThePersistedShape() {
        TransformationSpec spec = new ImageTransformationSpec(
                new LimitResolution(100, LimitResolution.Mode.KEEP_ASPECT_RATIO),
                null,
                new AspectRatio(1, 1, AspectRatio.Mode.FILL),
                new ImageEncoding.Webp(50)
        );

        var json = formatMapper.toString(spec, TransformationSpec.class);

        TransformationSpec roundTripped = formatMapper.fromString(json, TransformationSpec.class);
        assertThat(roundTripped).isEqualTo(spec);
    }

    @Test
    void aMinimalSpecRoundTripsToo() {
        TransformationSpec spec = new ImageTransformationSpec(null, null, null, new ImageEncoding.Jpeg(80));

        var json = formatMapper.toString(spec, TransformationSpec.class);

        TransformationSpec roundTripped = formatMapper.fromString(json, TransformationSpec.class);
        assertThat(roundTripped).isEqualTo(spec);
    }
}