package com.example.uploads_api.transformations.tasks;

import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import com.example.uploads_api.v2.transformations.operations.VideoTransformationOperations;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

@Builder
public record VideoTransformationTaskGroup(
        @NonNull ObjectLocation inputObject,
        @NonNull Collection<VideoTask> tasks,
        @NonNull UploadId uploadId,
        boolean async,
        String completedNotificationUrl,
        String progressNotificationUrl
) implements TransformationTaskGroup {
    @Builder
    public record VideoTask(
            @NonNull String name,
            @NonNull VideoTransformationOperations operations
    ) implements TransformationTask {
    }
}