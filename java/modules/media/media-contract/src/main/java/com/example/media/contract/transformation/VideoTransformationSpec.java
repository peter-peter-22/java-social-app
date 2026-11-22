package com.example.media.contract.transformation;

import java.util.Objects;

/**
 * The resolved operations of a video transformation.
 * Reserved for the video worker slice: it exists so the persisted polymorphic shape never needs a
 * rewrite. Do not grow it before the video worker arrives.
 */
public record VideoTransformationSpec(VideoEncoding encoding) implements TransformationSpec {
    public VideoTransformationSpec {
        Objects.requireNonNull(encoding, "encoding");
    }

    /**
     * The output encoding of a video transformation.
     * The placeholder variants mirror the prototype's names so the JSON discriminators stay stable.
     * Their fields arrive with the video worker slice.
     */
    public sealed interface VideoEncoding permits VideoEncoding.Mp4, VideoEncoding.Hls {

        /** Reserved: bitrate, frame rate, CRF, and preset fields arrive with the video worker. */
        record Mp4() implements VideoEncoding {
        }

        record Hls() implements VideoEncoding {
        }
    }
}