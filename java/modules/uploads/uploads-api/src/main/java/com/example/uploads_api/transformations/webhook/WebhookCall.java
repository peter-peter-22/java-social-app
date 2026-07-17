package com.example.uploads_api.transformations.webhook;

import com.example.uploads_api.uploads.UploadId;
import org.jetbrains.annotations.NotNull;

public record WebhookCall(
        @NotNull UploadId uploadId,
        @NotNull String transformationName
) {
}
