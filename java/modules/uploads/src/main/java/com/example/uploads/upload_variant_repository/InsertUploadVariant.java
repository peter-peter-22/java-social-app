package com.example.uploads.upload_variant_repository;

import org.jetbrains.annotations.NotNull;

public record InsertUploadVariant(
        @NotNull UploadVariantId key,
        @NotNull String bucketName,
        @NotNull String fileExtension,
        @NotNull UploadVariantStatus status
) {
}
