package com.example.uploads.registry.upload;

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
        String transformationGroup,
        Integer transformationVersion,
        UploadStatus status,
        String fileExtension
) {
}
