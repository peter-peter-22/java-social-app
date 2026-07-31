package com.example.uploads_api.transformations.webhook;

import com.example.uploads_api.uploads.UploadId;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record WebhookCall(
        @NonNull UploadId uploadId,
        @NonNull String transformationName
) {
}
