package com.example.uploads_service.api.transformations;

import com.example.image_transformer.api.transformations.operations.ImageTransformationOperations;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;

@Builder
@NullMarked
public final class ImageTransformationConfiguration {
    String name;
    ImageTransformationOperations operations;
}
