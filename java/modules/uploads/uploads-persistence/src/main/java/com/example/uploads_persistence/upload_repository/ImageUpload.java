package com.example.uploads_persistence.upload_repository;

import com.example.uploads_api.uploads.FileType;

import java.time.Instant;

public record ImageUpload(
        String homeRegion,
        String objectKey,
        String objectBucket,
        String uploadKey,
        FileType fileType,
        long version,
        Instant createdAt,
        int bytes,
        int width,
        int height,
        UploadStatus status
) {
}
