package com.example.uploads_persistence.upload_repository;

import org.springframework.data.relational.core.mapping.Table;

@Table("file_uploads")
record FileUploadEntity(
        String homeRegion,
        String assetId,
        String extension,
        long version,
        String mediaType,
        String createdAt,
        int bytes
) {
}
