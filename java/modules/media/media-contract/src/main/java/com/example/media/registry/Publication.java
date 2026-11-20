package com.example.media.registry;

import com.example.media.identity.AssetId;
import com.example.media.identity.AttemptId;
import com.example.media.identity.UploadKey;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@NullMarked
public record Publication(
        AttemptId attemptId,
        UploadKey uploadKey,
        AssetId originalAssetId,
        List<VariantRegistration> variants
) {
    public Publication {
        Objects.requireNonNull(attemptId, "attemptId");
        Objects.requireNonNull(uploadKey, "uploadKey");
        Objects.requireNonNull(originalAssetId, "originalAssetId");
        Objects.requireNonNull(variants, "variants");
        variants = List.copyOf(variants);
        Set<AssetId> assetIds = new HashSet<>();
        assetIds.add(originalAssetId);
        for (VariantRegistration variant : variants) {
            if (!assetIds.add(variant.assetId())) {
                throw new IllegalArgumentException("Each published asset id must be unique");
            }
        }
    }
}
