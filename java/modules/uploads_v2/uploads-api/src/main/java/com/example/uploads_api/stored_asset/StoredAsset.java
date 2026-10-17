package com.example.uploads_api.stored_asset;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@NullMarked
@Getter
@Builder
@EqualsAndHashCode
public final class StoredAsset{
    private final UUID id;
    private final String homeRegion;
    private final String objectBucket;
    private final String objectPath;
    private final boolean directory;
    @Nullable private final String entryPoint;
    private final String mimeType;
    private final String extension;
    private final Instant createdAt;
    private final Integer bytes;
    private final String deliveryProtocol;
    @Nullable private final ImageMetadata imageMetadata;
    @Nullable private final VideoMetadata videoMetadata;
}
