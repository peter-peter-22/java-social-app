package com.example.uploads_service.api.transformations;

import com.example.uploads_service.api.transformations.operations.ImageTransformationOperations;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;

@Builder
@NullMarked
@EqualsAndHashCode
@Getter
public final class NamedImageTransformation {
    String name;
    ImageTransformationOperations operations;
}
