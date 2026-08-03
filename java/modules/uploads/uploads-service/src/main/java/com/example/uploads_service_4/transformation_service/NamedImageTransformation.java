package com.example.uploads_service_4.transformation_service;

import com.example.uploads_api.v2.transformations.operations.ImageTransformationOperations;

public record NamedImageTransformation(
        String name,
        ImageTransformationOperations operations
) {
}
