package com.example.uploads.upload_repository;

import com.example.api.MediaType;
import com.example.api.UploadStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("uploads")
record UploadEntity(
        @Id UUID id,
        UUID createdBy,
        String bucketName,
        MediaType mediaType,
        Instant createdAt,
        UploadStatus status
        // region
) {
}
