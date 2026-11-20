package com.example.media.contract.asset;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * The data needed to register an asset.
 * The caller reserves {@link #id} before any object is written, because the id is the object prefix.
 * The status of a new asset is always {@link AssetStatus#UPLOADING}; metadata rows are attached
 * separately through the asset store.
 */
public record NewAsset(
        AssetId id,
        AssetPlacement placement,
        AssetDeliveryProtocol deliveryProtocol,
        String contentType,
        String entryPoint,
        @Nullable String extension,
        boolean directory,
        long bytes,
        List<String> objects
) {
    public NewAsset {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(placement, "placement");
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
    }
}