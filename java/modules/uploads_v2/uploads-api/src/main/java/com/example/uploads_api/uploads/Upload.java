package com.example.uploads_api.uploads;

import com.example.uploads_api.stored_asset.StoredAsset;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@EqualsAndHashCode
@NullMarked
public final class Upload {
    private final String uploadKey;
    private final UUID id;
    private final Instant createdAt;
    private final int bytes;
    private final UploadStatus status;
    private final StoredAsset storedAsset;
}
