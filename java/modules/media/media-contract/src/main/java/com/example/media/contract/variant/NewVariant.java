package com.example.media.contract.variant;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.transformation.TransformationVersionId;

import java.util.Objects;

/** The data needed to register a variant row. Registration cannot overwrite an existing variant. */
public record NewVariant(VariantId id, TransformationVersionId versionId, AssetId assetId) {
    public NewVariant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(versionId, "versionId");
        Objects.requireNonNull(assetId, "assetId");
    }
}