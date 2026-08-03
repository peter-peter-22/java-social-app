package com.example.uploads_persistence.upload_repository;

import org.springframework.data.relational.core.mapping.Table;

@Table("video_uploads")
record VideoUploadEntity(
        String homeRegion,
        String assetId,
        String extension,
        long version,
        String mediaType,
        String createdAt,
        int bytes,
        int width,
        int height,
        int lengthSeconds
) {
}
