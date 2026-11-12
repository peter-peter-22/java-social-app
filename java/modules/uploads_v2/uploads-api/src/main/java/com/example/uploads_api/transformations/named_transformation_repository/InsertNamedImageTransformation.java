package com.example.uploads_api.transformations.named_transformation_repository;

import com.example.uploads_api.transformations.ImageTransformationOperations;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record InsertNamedImageTransformation(
        String name,
        ImageTransformationOperations operations
) {
}