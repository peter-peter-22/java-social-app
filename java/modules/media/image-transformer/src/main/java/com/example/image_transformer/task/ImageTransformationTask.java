package com.example.image_transformer.task;

import com.example.media_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.uploads.ObjectLocation;
import com.example.media_api.uploads.UploadId;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;


public record ImageTransformationTask(
        @NonNull ImageTransformationOperations operations,
        @NotNull ObjectLocation inputObject,
        @NotNull ObjectLocation outputObject,
        @NotNull String name,
        boolean lazy,
        @NonNull UploadId uploadId
) {
}
