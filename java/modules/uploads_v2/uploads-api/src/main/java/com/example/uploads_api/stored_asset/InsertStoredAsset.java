package com.example.uploads_api.stored_asset;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@NullMarked
@Getter
@Builder
@EqualsAndHashCode
public class InsertStoredAsset {
    private final String homeRegion;
    private final String objectBucket;
    private final boolean directory;
    @Nullable private final String entryPoint;
    private final String mimeType;
   @Nullable private final String extension;
    private final long bytes;
    private final AssetDeliveryProtocol deliveryProtocol;
    private  final Instant expiration;
}
