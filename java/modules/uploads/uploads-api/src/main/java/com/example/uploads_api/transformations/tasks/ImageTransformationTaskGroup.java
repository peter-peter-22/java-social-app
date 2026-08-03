package com.example.uploads_api.transformations.tasks;

import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import com.example.uploads_api.v2.transformations.operations.ImageTransformationOperations;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

@Builder
public record ImageTransformationTaskGroup(
        @NonNull ObjectLocation inputObject,
        @NonNull Collection<ImageTask> tasks,
        @NonNull UploadId uploadId
) implements TransformationTaskGroup {
    @Builder
    public record ImageTask(
            @NonNull ObjectLocation outputObject,
            @NonNull String name,
            boolean async,
            @NonNull ImageTransformationOperations operations
    ) implements TransformationTask {
    }
}
