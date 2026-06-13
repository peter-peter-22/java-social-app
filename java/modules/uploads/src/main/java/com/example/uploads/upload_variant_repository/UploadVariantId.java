package com.example.uploads.upload_variant_repository;

import com.example.uploads.upload_repository.UploadId;
import org.jetbrains.annotations.NotNull;

public record UploadVariantId(
        @NotNull UploadId originId,
        @NotNull String variantName
) {
}
