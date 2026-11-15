package com.example.uploads_api.stored_asset;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@NullMarked
@Getter
@Builder
@Value
public class InsertStoredAsset {
    String homeRegion;
    String objectBucket;
    boolean directory;
    @Nullable
    String entryPoint;
    String mimeType;
    @Nullable
    String extension;
    long bytes;
    Collection<String> objects;
    AssetDeliveryProtocol deliveryProtocol;
}
