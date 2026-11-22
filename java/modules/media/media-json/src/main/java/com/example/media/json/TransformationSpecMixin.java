package com.example.media.json;

import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.transformation.TransformationSpec;
import com.example.media.contract.transformation.VideoTransformationSpec;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Carries the polymorphic JSON shape of {@link TransformationSpec} so the domain type stays free of
 * Jackson. The type property is {@code "type"}, and the subtype names are the stored discriminator
 * values. Do not rename them: rows in the registry and queued messages carry them.
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
abstract class TransformationSpecMixin {
}