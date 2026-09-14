package com.example.uploads_api.v2.uploads.upload_registry;

import com.example.uploads_api.uploads.UploadId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public final class InsertUploadVariant {
    private final UploadId originalId;
    private final String transformationName;
    private final String objectKeyPrefix;
    private final String mainObjectRelativePath;
    private final String mainObjectContentType;
}
