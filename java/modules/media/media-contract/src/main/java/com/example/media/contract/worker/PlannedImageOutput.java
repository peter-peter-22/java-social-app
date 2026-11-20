package com.example.media.contract.worker;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.object.ObjectPrefix;
import com.example.media.contract.transformation.ImageTransformationSpec;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * One planned result of an image transform.
 * The worker only writes under {@code prefix}; the API reserved the asset id and the prefix
 * before the command was sent.
 */
public record PlannedImageOutput(
        AssetId assetId,
        ObjectPrefix prefix,
        @Nullable String transformationName,
        ImageTransformationSpec spec
) {
    public PlannedImageOutput {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(spec, "spec");
    }

    /** Whether this is the incoming result that replaces the uploaded original as the upload's main asset. */
    public boolean isIncoming() {
        return transformationName == null;
    }
}