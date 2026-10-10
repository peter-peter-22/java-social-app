package com.example.uploads_api.uploads;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Builder
@Getter
@EqualsAndHashCode
@NullMarked
public final class InsertUpload {
    private final String homeRegion;
    private final String uploadKey;
    @Nullable
    private final String extension;
    private final String contentType;
    private final DisplayType mediaType;
    private final long bytes;
    private final UploadStatus status;
}
