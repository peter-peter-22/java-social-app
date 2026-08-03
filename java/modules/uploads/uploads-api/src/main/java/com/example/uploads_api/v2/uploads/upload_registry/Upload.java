package com.example.uploads_api.v2.uploads.upload_registry;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class Upload {
    private final String homeRegion;
    private final String objectKey;
    private final String objectBucket;
    private final String uploadKey;
    private final String extension;
    private final String contentType;
    private final long version;
    private final String createdAt;
    private final int bytes;
    private final UploadStatus status;
}
