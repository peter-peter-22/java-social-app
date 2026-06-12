package com.example.uploads.registry.upload_variant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("upload_variants")
public record UploadVariantEntity(
        @Id UploadVariantEntityId key,
        String bucketName,
        String fileExtension,
        Instant createdAt,
        UploadVariantStatus status
) {
}
