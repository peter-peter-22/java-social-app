package com.example.media.contract.worker;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.object.ObjectPrefix;

import java.util.List;
import java.util.Objects;

/**
 * One produced result of an image transform.
 * The paths are relative to the output prefix, exactly as they must be registered on the asset.
 * The worker allocates the asset id and prefix on every execution; the API validates them and
 * registers the asset only for the execution it accepts.
 */
public record CompletedImageOutput(
        AssetId assetId,
        ObjectPrefix prefix,
        String entryPoint,
        List<String> objects,
        long bytes,
        String contentType,
        ImageMetadata metadata
) {
    public CompletedImageOutput {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(prefix, "prefix");
        if (!prefix.keyPrefix().equals(assetId.value().toString()))
            throw new IllegalArgumentException(
                    "the prefix must be the asset id: " + prefix.keyPrefix() + " != " + assetId.value());
        Objects.requireNonNull(entryPoint, "entryPoint");
        Objects.requireNonNull(objects, "objects");
        objects = List.copyOf(objects);
        if (objects.isEmpty())
            throw new IllegalArgumentException("a produced output must contain at least one object");
        if (!objects.contains(entryPoint))
            throw new IllegalArgumentException("the entrypoint must be one of the produced objects: " + entryPoint);
        for (var object : objects)
            requireRelativeObjectPath(object);
        requireRelativeObjectPath(entryPoint);
        if (bytes < 0) throw new IllegalArgumentException("bytes must not be negative: " + bytes);
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(metadata, "metadata");
    }

    /**
     * The object paths are relative to the asset prefix. Absolute paths or parent segments would
     * escape the asset directory, so they are rejected before the API registers them.
     */
    private static void requireRelativeObjectPath(String path) {
        if (path.isBlank())
            throw new IllegalArgumentException("an object path must not be blank");
        if (path.startsWith("/") || path.contains("\\"))
            throw new IllegalArgumentException("an object path must be a relative path: " + path);
        for (var segment : path.split("/")) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals(".."))
                throw new IllegalArgumentException("an object path must not contain empty or parent segments: " + path);
        }
    }
}