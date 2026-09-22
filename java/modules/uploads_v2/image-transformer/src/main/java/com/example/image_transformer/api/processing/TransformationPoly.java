package com.example.image_transformer.api.processing;

import com.example.image_transformer.api.transformations.operations.ImageEncodings;
import com.example.image_transformer.api.transformations.operations.TransformationOperations;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class TransformationPoly {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ImageEncodings.Jpeg.class, name = "jpeg"),
            @JsonSubTypes.Type(value = ImageEncodings.Webp.class, name = "webp")
    })
    public sealed interface AnyTransformation<TransformationType extends TransformationOperations> permits  {
    }
}
