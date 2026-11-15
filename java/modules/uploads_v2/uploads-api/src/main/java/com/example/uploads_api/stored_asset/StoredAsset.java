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
    /**
     * The entrypoint is a relative path, it contains the extension.
     */
    private final String entryPoint;
    /**
     * The extension is also present in the entrypoint.
     * The extension is stored to make it easier to find and add it to the URL of the single object assets.
     * These assets don't add the entrypoint to their URL, so we need a separate way for getting the extension.
     * Making this a separate row is not necessary because it can be derived from the entrypoint.
     * This will might be removed.
     */
    @Nullable
    private final String extension;
    /**
     * This is the content type of the entrypoint file.
     * The content type that the client see when requesting a file is from the object storage, not this value.
     * An asset can hold multiple objects (e.g., HLS video), so a single content type cannot match all owned objects.
     * The main purpose of this field is to see what content type the original upload has, and record it for later use.
     */
    private final String contentType;
    private final Instant createdAt;
    private final long bytes;
    private final AssetDeliveryProtocol deliveryProtocol;
    private final StoredAssetStatus status;
    /**
     * The relative path of all objects in this asset, including the entrypoint.
     */
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

    /**
     * Return the object key based on the local path.
     */
    public String getObjectKey(String relativePath) {
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
