package com.example.uploads_persistence.upload_repository;

import com.example.uploads_api.uploads.FileType;
import com.example.uploads_api.uploads.UploadStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("uploads")
record UploadEntity(
        @Id UUID id,
        String objectPath,
        String bucket,
        UUID createdBy,
        FileType fileType,
        Instant createdAt,
        UploadStatus status
        // source region?
) {
}
