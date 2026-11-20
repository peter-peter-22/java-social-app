package com.example.media.contract.worker;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.object.ObjectPrefix;

import java.util.List;
import java.util.Objects;

/**
 * One produced result of an image transform.
 * The paths are relative to the output prefix, exactly as they must be registered on the asset.
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
        Objects.requireNonNull(entryPoint, "entryPoint");
        Objects.requireNonNull(objects, "objects");
        objects = List.copyOf(objects);
        if (objects.isEmpty())
            throw new IllegalArgumentException("a produced output must contain at least one object");
        if (!objects.contains(entryPoint))
            throw new IllegalArgumentException("the entrypoint must be one of the produced objects: " + entryPoint);
        if (bytes < 0) throw new IllegalArgumentException("bytes must not be negative: " + bytes);
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(metadata, "metadata");
    }
}