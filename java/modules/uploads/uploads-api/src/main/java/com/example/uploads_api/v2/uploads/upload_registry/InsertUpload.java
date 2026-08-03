package com.example.uploads_api.v2.uploads.upload_registry;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public final class InsertUpload {
    private final String homeRegion;
    private final String objectKey;
    private final String objectBucket;
    private final String uploadKey;
    private final String extension;
    private final String contentType;
    private final MediaType mediaType;
    private final long bytes;
    private final UploadStatus status;
}
