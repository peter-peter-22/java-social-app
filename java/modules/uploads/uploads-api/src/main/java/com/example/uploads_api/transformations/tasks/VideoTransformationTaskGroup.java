package com.example.uploads_api.transformations.tasks;

import com.example.uploads_api.transformations.operations.VideoTransformationOperations;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

@Builder
public record VideoTransformationTaskGroup(
        @NonNull ObjectLocation inputObject,
        @NonNull Collection<VideoTask> tasks,
        @NonNull UploadId uploadId
) implements TransformationTaskGroup {
    @Builder
    public record VideoTask(
            @NonNull ObjectLocation outputObject,
            @NonNull String name,
            boolean lazy,
            @NonNull VideoTransformationOperations operations
    ) implements TransformationTask {
    }
}