package com.example.media.json;

import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.TransformationSpec;
import com.example.media.contract.transformation.VideoEncoding;
import tools.jackson.databind.json.JsonMapper;

/**
 * The JSON configuration of the media service.
 *
 * <p>The contract types are plain Java; this module carries their JSON shape as Jackson mixins and
 * is the only place that knows Jackson. The web, the registry's jsonb columns, and the future
 * message queue all serialize through this configuration, so the shape cannot drift between them
 * and the planned format versioning has exactly one home: the polymorphic {@code type} property is
 * the versioning seam, later extended by a version field that is empty for the first version.</p>
 */
public final class MediaJson {

    private MediaJson() {
    }

    /**
     * Applies the media service's JSON shape to a mapper builder.
     */
    public static JsonMapper.Builder customize(JsonMapper.Builder builder) {
        return builder
                .addMixIn(TransformationSpec.class, TransformationSpecMixin.class)
                .addMixIn(ImageEncoding.class, ImageEncodingMixin.class)
                .addMixIn(VideoEncoding.class, VideoEncodingMixin.class)
                // The nested video records are plain domain types too, so their non-null shape is
                // pinned here instead of annotating the contract with Jackson.
                .addMixIn(VideoEncoding.VideoSettings.class, NonNullMixin.class)
                .addMixIn(VideoEncoding.AudioSettings.class, NonNullMixin.class)
                .addMixIn(VideoEncoding.Mp4.class, NonNullMixin.class)
                .addMixIn(VideoEncoding.Hls.class, NonNullMixin.class)
                .addMixIn(VideoEncoding.Hls.HlsTrack.class, NonNullMixin.class);
    }

    /**
     * A mapper that speaks the media service's JSON shape.
     */
    public static JsonMapper create() {
        return customize(JsonMapper.builder()).build();
    }
}