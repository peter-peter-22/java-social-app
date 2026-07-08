package com.example.uploads.upload_repository;

import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.UploadStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("uploads")
record UploadEntity(
        @Id UploadEntityId id,
        UUID createdBy,
        FileType fileType,
        Instant createdAt,
        UploadStatus status
        // region
) {
}
