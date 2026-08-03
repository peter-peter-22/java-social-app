package com.example.uploads_persistence.upload_repository;

import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("image_uploads")
record ImageUploadEntity(
        String homeRegion,
        String objectKey,
        String objectBucket,
        String assetId,
        String extension,
        long version,
        String mediaType,
        Instant createdAt,
        int bytes,
        int width,
        int height,
        String status
) {
}
