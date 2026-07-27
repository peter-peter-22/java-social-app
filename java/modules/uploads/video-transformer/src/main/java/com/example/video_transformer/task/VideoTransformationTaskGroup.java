package com.example.video_transformer.task;

import com.example.transformer_contracts.webhook.HasWebhookCall;
import com.example.uploads_api.transformations.operations.VideoTransformationOperations;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public record VideoTransformationTaskGroup(
        @NonNull ObjectLocation inputObject,
        @NonNull Collection<Task> tasks
) {
    @Builder
    public record Task(
            @NonNull VideoTransformationOperations operations,
            @NonNull ObjectLocation outputObject,
            @NonNull String name,
            boolean lazy,
            @NonNull UploadId uploadId
    ) implements HasWebhookCall {
    }
}
