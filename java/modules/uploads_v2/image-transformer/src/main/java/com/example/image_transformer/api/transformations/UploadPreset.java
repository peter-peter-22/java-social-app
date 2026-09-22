package com.example.image_transformer.api.transformations;

import com.example.image_transformer.api.authentication.AuthenticationType;
import com.example.image_transformer.api.transformations.operations.TransformationOperations;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Builder
@NullMarked
public final class UploadPreset<TransformationType extends TransformationOperations> {
    String name;
    NamedTransformation<TransformationType> transformation;
    @Nullable AuthenticationType authentication;
}
