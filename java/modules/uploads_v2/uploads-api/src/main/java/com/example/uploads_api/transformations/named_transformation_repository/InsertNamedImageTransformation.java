package com.example.uploads_api.transformations.named_transformation_repository;

import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.NullMarked;

@Builder
@NullMarked
@EqualsAndHashCode
public final class InsertNamedImageTransformation {
    String name;
    ImageTransformationOperations operations;
}
