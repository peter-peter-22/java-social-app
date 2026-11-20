package com.example.media.registry;

import com.example.media.asset.DeliveryProtocol;
import com.example.media.identity.AssetId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Contents known when the uploading row is inserted.
 * The caller chooses the id so the object prefix can be reserved before the row exists.
 * Region and bucket come from {@link com.example.media.storage.ObjectStore#placement()}.
 */
@NullMarked
public record NewAsset(
        AssetId id,
        String region,
        String bucket,
        boolean directory,
        String entryPoint,
        @Nullable String extension,
        String contentType,
        long bytes,
        DeliveryProtocol deliveryProtocol,
        List<String> objects
) {
    public NewAsset {
        Objects.requireNonNull(id, "id");
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region must not be blank");
        }
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("Bucket must not be blank");
        }
        if (entryPoint == null || entryPoint.isBlank()) {
            throw new IllegalArgumentException("Entry point must not be blank");
        }
        if (extension != null && extension.isBlank()) {
            throw new IllegalArgumentException("Extension must not be blank");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type must not be blank");
        }
        if (bytes < 0) {
            throw new IllegalArgumentException("Bytes must not be negative");
        }
        Objects.requireNonNull(deliveryProtocol, "deliveryProtocol");
        Objects.requireNonNull(objects, "objects");
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("An asset must register at least one object");
        }
        objects = List.copyOf(objects);
        for (String path : objects) {
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("Object paths must not be blank");
            }
        }
        if (!objects.contains(entryPoint)) {
            throw new IllegalArgumentException("Entry point must be one of the registered objects");
        }
        if (!directory && objects.size() != 1) {
            throw new IllegalArgumentException("A single-object asset must register exactly one object");
        }
    }
}
