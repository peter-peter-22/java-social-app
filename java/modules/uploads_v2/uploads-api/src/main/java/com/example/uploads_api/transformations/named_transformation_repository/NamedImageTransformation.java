package com.example.uploads_api.transformations.named_transformation_repository;

import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@Builder
@NullMarked
@EqualsAndHashCode
@Getter
public final class NamedImageTransformation {
    private String name;
    private ImageTransformationOperations operations;
    private UUID currentVersionId;
}
