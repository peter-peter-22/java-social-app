package com.example.media.contract.transformation;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * The output encoding of a video transformation.
 * The placeholder variants mirror the prototype's names so the JSON discriminators stay stable.
 * Their fields arrive with the video worker slice.
 */
public sealed interface VideoEncoding permits VideoEncoding.Mp4, VideoEncoding.Hls {

    /**
     * The content type of the entrypoint only.
     */
    String contentType();

    enum AudioCodec {
        aac
    }

    enum VideoCodec {
        libx264
    }

    /**
     * FFmpeg encoding quality presets.
     * Ranging from fastest and worst compression to slowest and best compression.
     * TODO The availability and the name of the presets is allegedly varying per hardware. This needs to be verified.
     */
    enum VideoEncodingPreset {
        /**
         * good for development tests
         */
        ultrafast,
        superfast,
        veryfast,
        faster,
        fast,
        /**
         * default
         */
        medium,
        /**
         * recommended for high compression rate
         */
        slow,
        slower,
        veryslow,
        placebo
    }

    /**
     * MP4 encoding parameters.
     *
     * @param constantRateFactor Target visual quality. Higher number means lower quality. 6 CRF points halves or doubles the estimated file size.
     *                           <p>- H.264 (libx264): default 23, sane range 18-28, visually lossless 17-18, mobile/web 25-27</p>
     *                           <p>- H.265/HEVC (libx265): default 28, sane range 23-32, high quality 23, web 30-32</p>
     * @param allowExperimental  Required for some video and audio encodings. //TODO list which encodings
     */
    @Builder
    record Mp4(
            @Nullable Integer targetSizeBytes,
            @Nullable Boolean disableSubtitles,
            @Nullable Integer audioChannels,
            @Nullable AudioCodec audioCodec,
            @Nullable Integer audioSampleRateHz,
            @Nullable Integer audioBitrate,
            @Nullable Integer videoFrameRate,
            @Nullable LimitResolution limitResolution,
            @Nullable Boolean allowExperimental,
            @Nullable Integer constantRateFactor,
            @Nullable VideoEncodingPreset encodingPreset,
            @Nullable Integer videoBitrate,
            @Nullable Boolean disableAudio,
            @Nullable VideoCodec videoCodec
    ) implements VideoEncoding {

        public Mp4 {
            if (targetSizeBytes != null && targetSizeBytes < 0)
                throw new IllegalArgumentException("targetSizeBytes cannot be negative");
            if (audioChannels != null && audioChannels < 0)
                throw new IllegalArgumentException("audioChannels cannot be negative");
            if (audioSampleRateHz != null && audioSampleRateHz < 0)
                throw new IllegalArgumentException("audioSampleRateHz cannot be negative");
            if (videoFrameRate != null && videoFrameRate < 0)
                throw new IllegalArgumentException("videoFrameRate cannot be negative");
            if(videoBitrate != null && videoBitrate < 0)
                throw new IllegalArgumentException("videoBitrate cannot be negative");
            if(constantRateFactor != null && constantRateFactor < 0)
                throw new IllegalArgumentException("constantRateFactor cannot be negative");
        }

        @Override
        public String contentType() {
            return "video/mp4";
        }
    }

    record Hls() implements VideoEncoding {
        @Override
        public String contentType() {
            return "";
        }
    }
}
