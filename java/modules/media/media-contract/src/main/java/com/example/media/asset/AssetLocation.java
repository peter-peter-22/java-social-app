package com.example.media.asset;

import com.example.media.identity.AssetId;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Objects;

/**
 * Where an asset's objects live. This is the only storage detail a worker receives.
 * The object key is {@code {assetId}/{relative path}} with {@code .} rewritten to {@code _},
 * matching the prototype's Minio key rule.
 */
@NullMarked
public record AssetLocation(
        String region,
        String bucket,
        AssetId assetId,
        List<String> relativePaths
) {
    public AssetLocation {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region must not be blank");
        }
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("Bucket must not be blank");
        }
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(relativePaths, "relativePaths");
        if (relativePaths.isEmpty()) {
            throw new IllegalArgumentException("An asset location must register at least one object");
        }
        relativePaths = List.copyOf(relativePaths);
        for (String path : relativePaths) {
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("Relative paths must not be blank");
            }
        }
    }

    public String objectKey(String relativePath) {
        if (!relativePaths.contains(relativePath)) {
            throw new IllegalArgumentException("Relative path is not registered on this asset");
        }
        return assetId.value() + "/" + relativePath.replace('.', '_');
    }

    public List<String> objectKeys() {
        return relativePaths.stream().map(this::objectKey).toList();
    }
}
