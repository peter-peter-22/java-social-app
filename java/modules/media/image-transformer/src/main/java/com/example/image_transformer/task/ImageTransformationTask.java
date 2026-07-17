package com.example.image_transformer.task;

import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@Builder
public record ImageTransformationTask(
        @NonNull ImageTransformationOperations operations,
        @NotNull ObjectLocation inputObject,
        @NotNull ObjectLocation outputObject,
        @NotNull String name,
        boolean lazy,
        @NonNull UploadId uploadId
) {
}
