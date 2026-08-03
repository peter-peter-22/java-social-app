package com.example.uploads_api.v2.uploads.upload_registry;

public record VariantFile(
        String relativePath,
        String contentType,
        String bytes
) {
}
