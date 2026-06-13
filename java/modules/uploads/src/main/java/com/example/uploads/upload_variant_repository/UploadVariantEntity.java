package com.example.uploads.upload_variant_repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("upload_variants")
record UploadVariantEntity(
        @Id UploadVariantEntityId key,
        String bucketName,
        String fileExtension,
        Instant createdAt,
        UploadVariantStatus status
) {
}
