package com.example.uploads_api.v2.uploads.upload_registry;

import com.example.uploads_api.uploads.UploadId;

import java.time.Instant;

public record UploadVariant(
        UploadId originalId,
        Instant createdAt,
        String transformationName,
        String objectKeyPrefix,
        String mainObjectRelativePath,
        String mainObjectContentType
) {
}
