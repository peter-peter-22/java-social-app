package com.example.image_transformer.task;

import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public record ImageTransformationTaskGroup(
        @NonNull ObjectLocation inputObject,
        @NonNull Collection<Task> tasks
) {
    @Builder
    public record Task(
            @NonNull ImageTransformationOperations operations,
            @NonNull ObjectLocation outputObject,
            @NonNull String name,
            boolean lazy,
            @NonNull UploadId uploadId
    ) {
    }
}
