package com.example.uploads_service.upload_service_2;

import java.util.List;

public record ImageUploadRequest(
        String uploadKey,
        String contentType,
        long bytes,
        List<String> eagerTransformations,
        String completionUrl,
        String progressUrl,
        boolean asyncEager
) {
}
