package com.example.media.registry.transformation;

import com.example.media.contract.transformation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The registry persists transformation specs in the jsonb column through the Jackson 2 databind,
 * while the wire format is read with Jackson 3. Both follow the annotations of the contract; this
 * test locks the persisted shape so a drift is caught without a database.
 */
class TransformationSpecPersistenceJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void imageSpecRoundTripsThroughThePersistedShape() throws Exception {
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
    void aMinimalSpecRoundTripsToo() throws Exception {
        TransformationSpec spec = new ImageTransformationSpec(null, null, null, new ImageEncoding.Jpeg(80));

        var json = objectMapper.writeValueAsString(spec);

        assertThat(objectMapper.readValue(json, TransformationSpec.class)).isEqualTo(spec);
    }
}
