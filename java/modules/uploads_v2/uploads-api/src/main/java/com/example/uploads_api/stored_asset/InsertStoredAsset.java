package com.example.uploads_api.stored_asset;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
@Getter
@Builder
@EqualsAndHashCode
public class InsertStoredAsset {
    private final String homeRegion;
    private final String objectBucket;
    private final String objectPath;
    private final boolean directory;
    private final String entryPoint;
    private final String mimeType;
    private final String extension;
    private final Integer bytes;
    private final String deliveryProtocol;
    @Nullable private final UUID imageMetadataId;
    @Nullable private final UUID videoMetadataId;
}
