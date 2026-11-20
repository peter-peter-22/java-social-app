package com.example.media.contract.transformation;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * The resolved operations of an image transformation.
 * Component names are the persisted JSON property names; do not rename them.
 */
public record ImageTransformationSpec(
        @Nullable LimitResolution limitWidth,
        @Nullable LimitResolution limitHeight,
        @Nullable AspectRatio aspectRatio,
        ImageEncoding encoding
) implements TransformationSpec {
    public ImageTransformationSpec {
        Objects.requireNonNull(encoding, "encoding");
    }
}