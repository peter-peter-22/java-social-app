package com.example.media.registry;

import com.example.media.identity.AssetId;
import com.example.media.identity.NamedTransformationId;
import com.example.media.identity.TransformationVersionId;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/** A variant to publish. The transformation is already resolved; this is only ids. */
@NullMarked
public record VariantRegistration(
        NamedTransformationId namedTransformationId,
        TransformationVersionId transformationVersionId,
        AssetId assetId
) {
    public VariantRegistration {
        Objects.requireNonNull(namedTransformationId, "namedTransformationId");
        Objects.requireNonNull(transformationVersionId, "transformationVersionId");
        Objects.requireNonNull(assetId, "assetId");
    }
}
