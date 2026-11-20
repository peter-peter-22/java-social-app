package com.example.media.contract.asset;

import com.example.media.contract.object.ObjectPrefix;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A registered asset: an immutable set of objects under one prefix.
 * Registered assets are immutable; new content means a new asset with a new id.
 */
public record Asset(
        AssetId id,
        AssetPlacement placement,
        AssetStatus status,
        AssetDeliveryProtocol deliveryProtocol,
        String contentType,
        String entryPoint,
        @Nullable String extension,
        boolean directory,
        long bytes,
        List<String> objects,
        @Nullable ImageMetadata image,
        @Nullable VideoMetadata video,
        Instant createdAt
) {
    public Asset {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(placement, "placement");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(deliveryProtocol, "deliveryProtocol");
        Objects.requireNonNull(contentType, "contentType");
        if (contentType.length() > 127)
            throw new IllegalArgumentException("contentType is longer than 127 characters");
        Objects.requireNonNull(entryPoint, "entryPoint");
        if (entryPoint.length() > 255)
            throw new IllegalArgumentException("entryPoint is longer than 255 characters");
        if (bytes < 0) throw new IllegalArgumentException("bytes must not be negative: " + bytes);
        Objects.requireNonNull(objects, "objects");
        objects = List.copyOf(objects);
        if (objects.isEmpty())
            throw new IllegalArgumentException("an asset must register at least one object");
        for (String relativePath : objects) {
            Objects.requireNonNull(relativePath, "objects element");
            if (relativePath.length() > 255)
                throw new IllegalArgumentException("object path is longer than 255 characters: " + relativePath);
        }
        if (!objects.contains(entryPoint))
            throw new IllegalArgumentException("the entrypoint must be one of the registered objects: " + entryPoint);
        Objects.requireNonNull(createdAt, "createdAt");
    }

    /**
     * The prefix that owns every object of this asset.
     * The asset id is the prefix, so keys of different assets cannot collide.
     */
    public ObjectPrefix prefix() {
        return new ObjectPrefix(placement.homeRegion(), placement.objectBucket(), id.value().toString());
    }
}