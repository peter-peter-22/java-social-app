package com.example.media.contract.upload;

import com.example.media.contract.asset.AssetId;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * The data needed to register a new attempt.
 * The transformation names are recorded for later resolution; the worker command carries the
 * resolved specs instead.
 */
public record NewUploadAttempt(
        UploadKey key,
        AssetId originalAssetId,
        List<String> eagerTransformationNames,
        @Nullable String incomingTransformationName
) {
    public NewUploadAttempt {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(originalAssetId, "originalAssetId");
        Objects.requireNonNull(eagerTransformationNames, "eagerTransformationNames");
        eagerTransformationNames = List.copyOf(eagerTransformationNames);
    }
}