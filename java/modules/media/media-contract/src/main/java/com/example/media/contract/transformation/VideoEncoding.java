package com.example.media.contract.transformation;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The output encoding of a video transformation.
 * The placeholder variants mirror the prototype's names so the JSON discriminators stay stable.
 *
 * <p>The stream-level settings are shared between MP4 and HLS: MP4 has one set of settings and HLS
 * uses the same types as the playlist-wide defaults that each track can partially override. HLS
 * muxer settings (segment length, segment type, playlist type) are not encoding settings and stay
 * on {@link Hls}.</p>
 */
public sealed interface VideoEncoding permits VideoEncoding.Mp4, VideoEncoding.Hls {

    /**
     * The content type of the entrypoint only.
     */
    String contentType();

    enum AudioCodec {
        AAC
    }

    enum VideoCodec {
        H264
    }

    /**
     * FFmpeg encoding quality presets.
     * Ranging from fastest and worst compression to slowest and best compression.
     * The names are the x264/x265 presets; they are not portable to every codec.
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
     * Stream-level settings shared by MP4 and the HLS playlist defaults.
     *
     * @param constantRateFactor Target visual quality. Higher number means lower quality. For H.264
     *                           the valid range is 0-51, the default is 23, and 6 CRF points halve
     *                           or double the estimated file size.
     * @param videoBitrate       Target video bitrate in bits per second. For HLS it is also the
     *                           value the master playlist's BANDWIDTH is derived from.
     * @param limitWidth         Limits the width while keeping the aspect ratio; never upscales.
     * @param limitHeight        Limits the height while keeping the aspect ratio; never upscales.
     * @param groupOfFrames      How many frames are between the i-frames aka. keyframes. Higher
     *                           number means smaller file size but slower seeking. It has a bigger
     *                           impact on HLS segments.
     * @param allowExperimental  Required for some video and audio encodings; maps to ffmpeg's
     *                           {@code -strict experimental}.
     */
    @Builder
    record VideoSettings(
            VideoCodec codec,
            @Nullable VideoEncodingPreset preset,
            @Nullable Integer constantRateFactor,
            @Nullable Integer videoBitrate,
            @Nullable Integer frameRate,
            @Nullable LimitResolution limitWidth,
            @Nullable LimitResolution limitHeight,
            @Nullable Integer groupOfFrames,
            @Nullable Boolean allowExperimental
    ) {
        public VideoSettings {
            Objects.requireNonNull(codec, "codec");
            if (constantRateFactor != null && (constantRateFactor < 0 || constantRateFactor > 51))
                throw new IllegalArgumentException("constantRateFactor must be between 0 and 51: " + constantRateFactor);
            if (videoBitrate != null && videoBitrate <= 0)
                throw new IllegalArgumentException("videoBitrate must be positive: " + videoBitrate);
            if (frameRate != null && (frameRate <= 0 || frameRate > 240))
                throw new IllegalArgumentException("frameRate must be between 1 and 240: " + frameRate);
            if (groupOfFrames != null && groupOfFrames <= 0)
                throw new IllegalArgumentException("groupOfFrames must be positive: " + groupOfFrames);
        }

        /**
         * The baseline H.264 settings; every other parameter is left at the encoder default.
         */
        public static VideoSettings h264() {
            return builder().codec(VideoCodec.H264).build();
        }
    }

    /**
     * Audio settings shared by MP4 and the HLS playlist defaults.
     *
     * @param bitrate      Target audio bitrate in bits per second.
     * @param channels     Number of audio channels.
     * @param sampleRateHz Audio sample rate in Hz.
     * @param disabled     When true the output has no audio stream.
     */
    @Builder
    record AudioSettings(
            AudioCodec codec,
            @Nullable Integer bitrate,
            @Nullable Integer channels,
            @Nullable Integer sampleRateHz,
            boolean disabled
    ) {
        public AudioSettings {
            Objects.requireNonNull(codec, "codec");
            if (bitrate != null && bitrate <= 0)
                throw new IllegalArgumentException("bitrate must be positive: " + bitrate);
            if (channels != null && (channels <= 0 || channels > 8))
                throw new IllegalArgumentException("channels must be between 1 and 8: " + channels);
            if (sampleRateHz != null && sampleRateHz <= 0)
                throw new IllegalArgumentException("sampleRateHz must be positive: " + sampleRateHz);
        }

        /**
         * AAC with the encoder defaults.
         */
        public static AudioSettings aac() {
            return builder().codec(AudioCodec.AAC).build();
        }

        /**
         * No audio stream.
         */
        public static AudioSettings noAudio() {
            return builder().codec(AudioCodec.AAC).disabled(true).build();
        }
    }

    /**
     * MP4 encoding.
     *
     * @param fastStart When true (the default when null) the moov atom is written before the media
     *                  data, so a browser can start playback before the whole file is downloaded.
     */
    @Builder
    record Mp4(
            VideoSettings video,
            AudioSettings audio,
            @Nullable Boolean fastStart
    ) implements VideoEncoding {

        public Mp4 {
            Objects.requireNonNull(video, "video");
            Objects.requireNonNull(audio, "audio");
        }

        @Override
        public String contentType() {
            return "video/mp4";
        }
    }

    /**
     * HLS encoding. The playlist is an fMP4/CMAF VOD playlist.
     *
     * <p>The tracks are the variants of the master playlist. A null track field inherits the
     * playlist-wide value from {@link #video()} or {@link #audio()}. Every track must resolve to a
     * video bitrate, because the master playlist's BANDWIDTH is derived from it.</p>
     *
     * @param segmentSeconds Target segment length in seconds.
     */
    @Builder
    record Hls(
            VideoSettings video,
            AudioSettings audio,
            int segmentSeconds,
            List<HlsTrack> tracks
    ) implements VideoEncoding {
        private static final Pattern TRACK_NAME = Pattern.compile("[A-Za-z0-9_-]{1,64}");

        public Hls {
            Objects.requireNonNull(video, "video");
            Objects.requireNonNull(audio, "audio");
            if (segmentSeconds <= 0 || segmentSeconds > 60)
                throw new IllegalArgumentException("segmentSeconds must be between 1 and 60: " + segmentSeconds);
            Objects.requireNonNull(tracks, "tracks");
            tracks = List.copyOf(tracks);
            if (tracks.isEmpty())
                throw new IllegalArgumentException("tracks cannot be empty");
            if (tracks.size() > 10)
                throw new IllegalArgumentException("tracks cannot contain more than 10 entries");

            var names = new HashSet<String>();
            for (var track : tracks) {
                if (!names.add(track.name()))
                    throw new IllegalArgumentException("duplicate track name: " + track.name());
                if (track.audioBitrate() != null && audio.disabled())
                    throw new IllegalArgumentException("track " + track.name() + " sets audioBitrate but audio is disabled");
                if (track.videoBitrate() == null && video.videoBitrate() == null)
                    throw new IllegalArgumentException(
                            "track " + track.name() + " needs a videoBitrate (its own or the playlist default)");
            }
        }

        @Override
        public String contentType() {
            return "application/vnd.apple.mpegurl";
        }

        /**
         * One HLS variant. Null fields inherit the playlist-wide defaults.
         * The name is expanded into the variant's directory name, so it must be filesystem-safe.
         */
        @Builder
        public record HlsTrack(
                String name,
                @Nullable LimitResolution limitWidth,
                @Nullable LimitResolution limitHeight,
                @Nullable Integer constantRateFactor,
                @Nullable Integer videoBitrate,
                @Nullable Integer audioBitrate
        ) {
            public HlsTrack {
                Objects.requireNonNull(name, "name");
                if (!TRACK_NAME.matcher(name).matches())
                    throw new IllegalArgumentException("name must match [A-Za-z0-9_-]{1,64}: " + name);
                if (constantRateFactor != null && (constantRateFactor < 0 || constantRateFactor > 51))
                    throw new IllegalArgumentException(
                            "constantRateFactor must be between 0 and 51: " + constantRateFactor);
                if (videoBitrate != null && videoBitrate <= 0)
                    throw new IllegalArgumentException("videoBitrate must be positive: " + videoBitrate);
                if (audioBitrate != null && audioBitrate <= 0)
                    throw new IllegalArgumentException("audioBitrate must be positive: " + audioBitrate);
            }
        }
    }
}
