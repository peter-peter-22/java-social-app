package com.example.media.contract.transformation;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * The resolved operations of a video transformation.
 * Reserved for the video worker slice: it exists so the persisted polymorphic shape never needs a
 * rewrite.
 */
public record VideoTransformationSpec(VideoEncoding encoding) implements TransformationSpec {
    // Unlike in the case of the image transformation, there is nothing between the encoding and decoding.
    //  So there aren't any shared transformation operations.
    //  Everything is decided by the encoding parameters.
    //
    // Here, the parameters those are shared between all encodings could be collected, e.g., "encoding preset".
    public VideoTransformationSpec {
        Objects.requireNonNull(encoding, "encoding");
    }
}