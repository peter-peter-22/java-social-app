package com.example.media_api.transformations.api;

import com.example.media_api.uploads.UploadId;
import org.jetbrains.annotations.NotNull;

public record WebhookDTO(
        @NotNull String transformationName,
        @NotNull UploadId uploadId
) {
}
