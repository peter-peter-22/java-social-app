package com.example.media.contract.worker;

import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.transformation.ImageTransformationSpec;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * One planned result of an image transform.
 *
 * <p>The command does not carry a result asset id. The worker allocates a fresh
 * {@link com.example.media.contract.asset.AssetId} for this output on every execution, under the
 * given placement, and reports it in the result. A retry therefore writes to new object keys and
 * cannot collide with the objects of a previous, partially completed execution. The API registers
 * the reported assets only for the execution it accepts.</p>
 */
public record PlannedImageOutput(
        @Nullable String transformationName,
        AssetPlacement placement,
        ImageTransformationSpec spec
) {
    public PlannedImageOutput {
        Objects.requireNonNull(placement, "placement");
        Objects.requireNonNull(spec, "spec");
    }

    /** Whether this is the incoming result that replaces the uploaded original as the upload's main asset. */
    public boolean isIncoming() {
        return transformationName == null;
    }
}