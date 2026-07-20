package com.example.uploads_api.transformations.webhook;

import com.example.uploads_api.uploads.UploadId;
import org.jspecify.annotations.NonNull;

public record WebhookCall(
        @NonNull UploadId uploadId,
        @NonNull String transformationName
) {
}
