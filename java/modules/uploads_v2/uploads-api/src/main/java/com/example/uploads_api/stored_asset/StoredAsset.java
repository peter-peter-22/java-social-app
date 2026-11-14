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
    private final String objectBucket; // TODO instead of deriving the object key from the asset id and using a fixed object bucket, generate them on insert and record them from the database to make this more migration friendly
    private final String objectKey;
    private final boolean directory;
    /** The entrypoint is a relative path, it contains the extension. */
    private final String entryPoint;
    private final String mimeType;
    @Nullable
    private final String extension;
    private final String contentType; // TODO add the content type to the underlying entity repository and SQL
    private final Instant createdAt;
    private final long bytes;
    private final String deliveryProtocol;
    private final StoredAssetStatus status;
    /** The relative path of all objects in this asset, including the entrypoint. */
    private final Collection<String> objects; // TODO sync with SQL and entities
    @Nullable
    private final ImageMetadata imageMetadata;
    @Nullable
    private final VideoMetadata videoMetadata;

    /**
     * The contents of a stored asset are relative to this object key prefix.
     */
    public String getDirectoryPrefix() {
        return id.toString();
    } // TODO replace with row field access

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
