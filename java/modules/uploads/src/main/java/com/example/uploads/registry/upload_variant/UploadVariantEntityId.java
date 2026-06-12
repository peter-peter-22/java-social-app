package com.example.uploads.registry.upload_variant;

import java.util.UUID;

public record UploadVariantEntityId(
        UUID originId,
        String variantName
) {
}
