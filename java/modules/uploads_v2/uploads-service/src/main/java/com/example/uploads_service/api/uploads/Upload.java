package com.example.uploads_service.api.uploads;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Builder
@Getter
@EqualsAndHashCode
@NullMarked
public final class Upload {
    private final String homeRegion;
    private final String uploadKey;
    @Nullable
    private final String extension;
    private final String contentType;
    private final Instant createdAt;
    private final Instant lastModified;
    private final int bytes;
    private final UploadStatus status;
}
