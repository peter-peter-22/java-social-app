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
    String contentType;
    @Nullable
    String extension;
    long bytes;
    // TODO giving a null value to this does not causes error when using the builder. it doesn't show a null safety warning either. something might be wrong with jspecify or the builder.
    Collection<String> objects;
    AssetDeliveryProtocol deliveryProtocol;
}
