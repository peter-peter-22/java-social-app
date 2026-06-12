package com.example.uploads.registry.upload_variant;

import com.example.uploads.registry.UploadId;
import org.jetbrains.annotations.NotNull;

public record UploadVariantKey(
        @NotNull UploadId originId,
        @NotNull String variantName
) {
}
