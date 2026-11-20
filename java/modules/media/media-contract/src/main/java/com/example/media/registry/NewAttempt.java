package com.example.media.registry;

import com.example.media.identity.AssetId;
import com.example.media.identity.UploadKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@NullMarked
public record NewAttempt(
        UploadKey uploadKey,
        List<String> eagerTransformationNames,
        @Nullable String incomingTransformationName,
        AssetId originalAssetId
) {
    public NewAttempt {
        Objects.requireNonNull(uploadKey, "uploadKey");
        Objects.requireNonNull(eagerTransformationNames, "eagerTransformationNames");
        eagerTransformationNames = List.copyOf(eagerTransformationNames);
        for (String name : eagerTransformationNames) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Eager transformation names must not be blank");
            }
        }
        if (incomingTransformationName != null && incomingTransformationName.isBlank()) {
            throw new IllegalArgumentException("Incoming transformation name must not be blank");
        }
        Objects.requireNonNull(originalAssetId, "originalAssetId");
    }
}
