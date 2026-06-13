package com.example.uploads.upload_variant_repository;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record UploadVariant(
        @NotNull UploadVariantId key,
        @NotNull String bucketName,
        @NotNull String fileExtension,
        @NotNull Instant createdAt,
        @NotNull UploadVariantStatus status
) {
}
