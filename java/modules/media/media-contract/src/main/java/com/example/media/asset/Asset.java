package com.example.media.asset;

import com.example.media.identity.AssetId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@NullMarked
public record Asset(
        AssetId id,
        String region,
        String bucket,
        boolean directory,
        String entryPoint,
        @Nullable String extension,
        String contentType,
        Instant createdAt,
        long bytes,
        DeliveryProtocol deliveryProtocol,
        AssetStatus status,
        List<String> objects,
        @Nullable ImageMetadata imageMetadata,
        @Nullable VideoMetadata videoMetadata
) {
    public Asset {
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
        Objects.requireNonNull(createdAt, "createdAt");
        if (bytes < 0) {
            throw new IllegalArgumentException("Bytes must not be negative");
        }
        Objects.requireNonNull(deliveryProtocol, "deliveryProtocol");
        Objects.requireNonNull(status, "status");
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

    public AssetLocation location() {
        return new AssetLocation(region, bucket, id, objects);
    }
}
