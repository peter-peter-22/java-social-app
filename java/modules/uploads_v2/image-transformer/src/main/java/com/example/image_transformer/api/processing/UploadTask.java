package com.example.image_transformer.api.processing;

import com.example.image_transformer.api.authentication.AuthenticationType;
import com.example.image_transformer.api.transformations.operations.ImageTransformationOperations;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@Builder
@NullMarked
@EqualsAndHashCode
@Getter
public final class UploadTask<TransformationType> {
    String uploadKey;
    Collection<ImageTransformationOperations> transformations;
}
