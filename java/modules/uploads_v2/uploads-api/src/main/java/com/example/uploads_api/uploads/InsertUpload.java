package com.example.uploads_api.uploads;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@Builder
@Getter
@EqualsAndHashCode
@NullMarked
public final class InsertUpload {
    private final String uploadKey;
    private final int bytes;
    private final UploadStatus status;
    private final UUID storedAssetId;
}
