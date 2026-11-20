package com.example.media.worker;

import com.example.media.asset.AssetLocation;
import com.example.media.identity.AssetId;
import com.example.media.identity.TransformationVersionId;
import com.example.media.transformation.ImageTransformation;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * A result the API has already assigned to an asset id and object prefix.
 * The worker fills that prefix. It does not choose the id or look up the transformation name.
 */
@NullMarked
public record ReservedImageResult(
        AssetId assetId,
        AssetLocation location,
        TransformationVersionId versionId,
        String transformationName,
        ImageTransformation transformation
) {
    public ReservedImageResult {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(location, "location");
        if (!assetId.equals(location.assetId())) {
            throw new IllegalArgumentException("Reserved asset id must match its location");
        }
        if (location.relativePaths().size() != 1 || transformation == null || transformation.encoding().directory()) {
            throw new IllegalArgumentException("An image result must be a single object");
        }
        Objects.requireNonNull(versionId, "versionId");
        if (transformationName == null || transformationName.isBlank()) {
            throw new IllegalArgumentException("Transformation name must not be blank");
        }
    }
}
