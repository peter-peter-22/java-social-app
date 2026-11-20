package com.example.media.variant;

import com.example.media.identity.AssetId;
import com.example.media.identity.TransformationVersionId;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.Objects;

@NullMarked
public record Variant(
        VariantId id,
        TransformationVersionId transformationVersionId,
        Instant createdAt,
        AssetId assetId
) {
    public Variant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(transformationVersionId, "transformationVersionId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(assetId, "assetId");
    }
}
