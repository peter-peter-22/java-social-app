package com.example.uploads_api.stored_asset;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@NullMarked
@Getter
@Builder
@EqualsAndHashCode
public final class StoredAsset {
    private final UUID id;
    private final String homeRegion;
    private final String objectBucket;
    private final boolean directory;
    /** The entrypoint is a relative path, it contains the extension. */
    private final String entryPoint;
    private final String mimeType;
    @Nullable
    private final String extension;
    private final String contentType;
    private final Instant createdAt;
    private final long bytes;
    private final AssetDeliveryProtocol deliveryProtocol;
    private final StoredAssetStatus status;
    /** The relative path of all objects in this asset, including the entrypoint. */
    private final Collection<String> objects;
    @Nullable
    private final ImageMetadata imageMetadata;
    @Nullable
    private final VideoMetadata videoMetadata;

    /**
     * The contents of a stored asset are relative to this object key prefix.
     */
    public String getDirectoryPrefix() {
        return id.toString();
    }

    /** Return the object key based on the local path. */
    public String getObjectKey(String relativePath){
        return getDirectoryPrefix() + "/" + relativePath;
    }

    /**
     * The object key of the entrypoint.
     */
    public String getMainObjectKey() {
        return getObjectKey(entryPoint);
    }

    /**
     * Return all object keys of this asset, including the entrypoint.
     */
    public Collection<String> getAllObjectKeys() {
        return objects.stream().map(this::getObjectKey).toList();
    }
}
