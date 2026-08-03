package com.example.uploads_api.v2.uploads.upload_object;

import java.nio.file.Path;

public record UploadVariantObjectArgs(
        String uploadKey, String variantName, String relativePath, Path source
) {
}
