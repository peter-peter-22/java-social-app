package com.example.media.contract.transformation;

import java.util.Objects;

/**
 * The resolved operations of a video transformation.
 *
 * <p>Unlike an image transformation there is no separate crop/pad operation before the encoding:
 * the video is decoded, the resolution limits are applied, and the result is encoded. The limits
 * therefore live in the encoding settings ({@link VideoEncoding.VideoSettings} and the HLS tracks),
 * because every HLS variant can have its own limit.</p>
 */
public record VideoTransformationSpec(VideoEncoding encoding) implements TransformationSpec {
    public VideoTransformationSpec {
        Objects.requireNonNull(encoding, "encoding");
    }
}