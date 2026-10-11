package com.example.uploads_api.transformations.operations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ImageTransformationOperations.class, name = "image"),
        @JsonSubTypes.Type(value = VideoTransformationOperations.class, name = "webp")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface PolymorphicTransformationOperations
        extends TransformationOperations
        permits ImageTransformationOperations, VideoTransformationOperations {
}
