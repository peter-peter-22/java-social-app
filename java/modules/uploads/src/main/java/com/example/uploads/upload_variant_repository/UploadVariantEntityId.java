package com.example.uploads.upload_variant_repository;

import java.util.UUID;

record UploadVariantEntityId(
        UUID originId,
        String variantName
) {
}
