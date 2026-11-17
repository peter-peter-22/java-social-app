package com.example.uploads_api.transformations.named_transformation_repository;

import com.example.uploads_api.transformations.ImageTransformationOperations;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@Builder
@NullMarked
@Getter
@Value
public class NamedImageTransformation {
    UUID id;
    String name;
    ImageTransformationOperations operations;
    UUID currentVersionId;
}
