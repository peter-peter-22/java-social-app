package com.example.media.json;

import com.example.media.contract.transformation.VideoTransformationSpec;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Carries the polymorphic JSON shape of {@link VideoTransformationSpec.VideoEncoding} so the domain
 * type stays free of Jackson. The type property is {@code "type"}, and the subtype names are the
 * stored discriminator values. Do not rename them: rows in the registry and queued messages carry
 * them.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = VideoTransformationSpec.VideoEncoding.Mp4.class, name = "mp4"),
        @JsonSubTypes.Type(value = VideoTransformationSpec.VideoEncoding.Hls.class, name = "hls")
})
abstract class VideoEncodingMixin {
}