package com.example.media.worker;

import com.example.media.asset.ImageMetadata;
import com.example.media.identity.AssetId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/** Objects the worker wrote under a reserved prefix, plus the metadata the API must store. */
@NullMarked
public record StoredObjectResult(
        AssetId assetId,
        String contentType,
        @Nullable String extension,
        long bytes,
        List<String> relativePaths,
        String entryPoint,
        ImageMetadata metadata
) {
    public StoredObjectResult {
        Objects.requireNonNull(assetId, "assetId");
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type must not be blank");
        }
        if (extension != null && extension.isBlank()) {
            throw new IllegalArgumentException("Extension must not be blank");
        }
        if (bytes < 0) {
            throw new IllegalArgumentException("Bytes must not be negative");
        }
        Objects.requireNonNull(relativePaths, "relativePaths");
        if (relativePaths.isEmpty()) {
            throw new IllegalArgumentException("A result must list the objects it wrote");
        }
        relativePaths = List.copyOf(relativePaths);
        for (String path : relativePaths) {
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("Relative paths must not be blank");
            }
        }
        if (entryPoint == null || !relativePaths.contains(entryPoint)) {
            throw new IllegalArgumentException("Entry point must be one of the produced objects");
        }
        Objects.requireNonNull(metadata, "metadata");
    }
}
