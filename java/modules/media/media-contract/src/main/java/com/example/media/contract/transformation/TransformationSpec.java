package com.example.media.contract.transformation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The resolved operations of a transformation.
 * The JSON shape is persisted in the registry (jsonb) and sent to workers; it is part of this
 * contract and must not drift: the type property is "type", and the subtype names below are the
 * stored discriminator values.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ImageTransformationSpec.class, name = "image"),
        @JsonSubTypes.Type(value = VideoTransformationSpec.class, name = "video")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface TransformationSpec permits ImageTransformationSpec, VideoTransformationSpec {
}