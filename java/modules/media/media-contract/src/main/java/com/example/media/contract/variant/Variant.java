package com.example.media.contract.variant;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.transformation.TransformationVersionId;

import java.time.Instant;
import java.util.Objects;

/**
 * A named transformed result of an upload.
 * The variant pins the transformation version it was produced from, so later edits of the named
 * transformation never change existing variants.
 */
public record Variant(VariantId id, TransformationVersionId versionId, AssetId assetId, Instant createdAt) {
    public Variant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(versionId, "versionId");
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}