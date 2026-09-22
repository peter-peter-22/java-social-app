package com.example.image_transformer.api.transformations;

import com.example.image_transformer.api.processing.TransformationPoly;
import com.example.image_transformer.api.transformations.operations.TransformationOperations;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;

@Builder
@NullMarked
public final class NamedTransformation<TransformationType extends TransformationOperations> extends TransformationPoly {
    String name;
    @Builder.Default
    boolean explicit=false;
    TransformationType operations;
}
