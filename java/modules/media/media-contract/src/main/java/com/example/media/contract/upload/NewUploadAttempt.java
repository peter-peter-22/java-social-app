package com.example.media.contract.upload;

import com.example.media.contract.asset.AssetId;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * The data needed to register a new attempt.
 * The transformation names are recorded for later resolution; the worker command carries the
 * resolved specs instead. The visibility is recorded because async completion reloads the attempt
 * and must know how the resulting upload is read. The permission is the one the attempt consumed,
 * if any: a terminal failure revokes its remaining uses.
 */
public record NewUploadAttempt(
        UploadKey key,
        AssetId originalAssetId,
        List<String> eagerTransformationNames,
        @Nullable String incomingTransformationName,
        UploadVisibility visibility,
        @Nullable UploadPermissionId uploadPermissionId
) {
    public NewUploadAttempt {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(originalAssetId, "originalAssetId");
        Objects.requireNonNull(eagerTransformationNames, "eagerTransformationNames");
        eagerTransformationNames = List.copyOf(eagerTransformationNames);
        Objects.requireNonNull(visibility, "visibility");
    }
}