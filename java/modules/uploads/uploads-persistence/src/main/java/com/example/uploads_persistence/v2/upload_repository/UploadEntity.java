package com.example.uploads_persistence.v2.upload_repository;

import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("uploads")
record UploadEntity(
        UUID fileId,
        String homeRegion,
        String uploadKey,
        String extension,
        String contentType,
        long version,
        String mediaType,
        String createdAt,
        int bytes
) {
}
