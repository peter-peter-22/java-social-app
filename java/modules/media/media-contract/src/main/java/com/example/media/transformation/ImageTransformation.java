package com.example.media.transformation;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/** Resolved image operations. Absent limits mean that step is skipped. */
@NullMarked
public record ImageTransformation(
        @Nullable LimitResolution limitWidth,
        @Nullable LimitResolution limitHeight,
        @Nullable AspectRatio aspectRatio,
        ImageEncoding encoding
) {
    public ImageTransformation {
        Objects.requireNonNull(encoding, "encoding");
    }
}
