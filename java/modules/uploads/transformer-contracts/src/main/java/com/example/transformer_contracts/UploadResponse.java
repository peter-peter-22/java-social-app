package com.example.transformer_contracts;

import java.util.Collection;

public record UploadResponse(
        String assetId,
        String extension,
        long version,
        String mediaType,
        String createdAt,
        int bytes,
        int width,
        int height,
        Collection<UploadVariant> variants
) {
}
