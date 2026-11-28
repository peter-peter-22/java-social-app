package com.example.media.video_worker.transform;

import com.example.media.contract.transformation.LimitResolution;
import com.example.media.contract.transformation.VideoEncoding;
import com.example.media.contract.transformation.VideoTransformationSpec;
import net.bramp.ffmpeg.builder.AbstractFFmpegOutputBuilder;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.HlsVariant;
import net.bramp.ffmpeg.builder.Strict;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Maps a resolved video transformation to the ffmpeg command line. The class is deliberately free
 * of process execution so the argument shape can be unit tested without an ffmpeg binary; the
 * parser of the last resort is still {@link VideoProcessor}.
 *
 * <p>Per-stream settings are not exposed by {@code net.bramp.ffmpeg}'s builder API, so the HLS
 * variants use output-level extra arguments (stream specifiers such as {@code -b:v:0} and
 * {@code -map}). Keeping all of that syntax here is what keeps it out of the domain.</p>
 */
final class FfmpegCommandBuilder {

    static final String MP4_ENTRY_POINT = "result.mp4";
    static final String HLS_MASTER_PLAYLIST = "master.m3u8";
    private static final String HLS_VARIANT_PLAYLIST = "var-%v/index.m3u8";
    private static final String HLS_SEGMENT_FILE = "var-%v/segment-%d.m4s";

    /**
     * The entrypoint of the output and the builder that writes it.
     */
    record BuiltCommand(FFmpegBuilder builder, Path entryPoint) {
    }

    BuiltCommand build(
            Path source,
            Path outputDir,
            VideoTransformationSpec spec,
            boolean hasAudio,
            int sourceFrameRate
    ) {
        return switch (spec.encoding()) {
            case VideoEncoding.Mp4 mp4 -> mp4(source, outputDir, mp4, hasAudio);
            case VideoEncoding.Hls hls -> hls(source, outputDir, hls, hasAudio, sourceFrameRate);
        };
    }

    private BuiltCommand mp4(Path source, Path outputDir, VideoEncoding.Mp4 mp4, boolean hasAudio) {
        var entryPoint = outputDir.resolve(MP4_ENTRY_POINT);
        var output = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .setInput(source)
                .done()
                .addOutput(entryPoint);

        applyCommonVideo(output, mp4.video(), true);
        var filter = scaleFilter(mp4.video().limitWidth(), mp4.video().limitHeight());
        if (filter != null)
            output.setVideoFilter(filter);

        if (hasAudio && !mp4.audio().disabled())
            applyAudio(output, mp4.audio(), true);
        else
            output.disableAudio();
        output.addExtraArgs("-map", "0:v:0");
        if (hasAudio && !mp4.audio().disabled())
            output.addExtraArgs("-map", "0:a:0?");
        if (!Boolean.FALSE.equals(mp4.fastStart()))
            output.setVideoMovFlags("+faststart");

        return new BuiltCommand(output.done(), entryPoint);
    }

    private BuiltCommand hls(
            Path source,
            Path outputDir,
            VideoEncoding.Hls hls,
            boolean hasAudio,
            int sourceFrameRate
    ) {
        var entryPoint = outputDir.resolve(HLS_MASTER_PLAYLIST);
        var output = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .setInput(source)
                .done()
                .addHlsOutput(outputDir.resolve(HLS_VARIANT_PLAYLIST));

        output.setHlsTime(hls.segmentSeconds(), TimeUnit.SECONDS)
                .setHlsSegmentFileName(outputDir.resolve(HLS_SEGMENT_FILE).toString())
                .setHlsPlaylistType("vod")
                .setHlsListSize(0)
                .setMasterPlName(HLS_MASTER_PLAYLIST)
                .addExtraArgs("-hls_segment_type", "fmp4", "-hls_flags", "independent_segments");

        // The rate control is emitted per variant below so a track can override the defaults
        // without relying on ffmpeg's precedence between -crf and -crf:v:N.
        applyCommonVideo(output, hls.video(), false);

        var frameRate = hls.video().frameRate() != null
                ? hls.video().frameRate()
                : Math.max(1, sourceFrameRate);
        var groupOfFrames = hls.video().groupOfFrames() != null
                ? hls.video().groupOfFrames()
                : Math.max(1, hls.segmentSeconds() * frameRate);
        output.addExtraArgs("-g", Integer.toString(groupOfFrames), "-sc_threshold", "0");

        var hasTrackedAudio = hasAudio && !hls.audio().disabled();
        if (hasTrackedAudio)
            applyAudio(output, hls.audio(), false);
        else
            output.disableAudio();

        var scaling = hls.tracks().stream().anyMatch(track ->
                effectiveLimit(track.limitWidth(), hls.video().limitWidth()) != null
                        || effectiveLimit(track.limitHeight(), hls.video().limitHeight()) != null);
        if (scaling)
            output.setComplexFilter(filterGraph(hls));

        for (int i = 0; i < hls.tracks().size(); i++) {
            var track = hls.tracks().get(i);
            var variant = new HlsVariant().addVideo(i).setName(track.name());
            if (hasTrackedAudio)
                variant.addAudio(i);
            output.addVariant(variant);

            output.addExtraArgs("-map", scaling ? "[v" + i + "out]" : "0:v:0");
            if (hasTrackedAudio)
                output.addExtraArgs("-map", "0:a:0");

            var videoBitrate = Objects.requireNonNull(
                    track.videoBitrate() != null ? track.videoBitrate() : hls.video().videoBitrate(),
                    "the domain guarantees a video bitrate for every HLS track");
            output.addExtraArgs("-b:v:" + i, videoBitrate.toString());

            var constantRateFactor = track.constantRateFactor() != null
                    ? track.constantRateFactor()
                    : hls.video().constantRateFactor();
            if (constantRateFactor != null)
                output.addExtraArgs("-crf:v:" + i, constantRateFactor.toString());

            if (hasTrackedAudio) {
                var audioBitrate = track.audioBitrate() != null
                        ? track.audioBitrate()
                        : hls.audio().bitrate();
                if (audioBitrate != null)
                    output.addExtraArgs("-b:a:" + i, audioBitrate.toString());
            }
        }

        return new BuiltCommand(output.done(), entryPoint);
    }

    private static void applyCommonVideo(
            AbstractFFmpegOutputBuilder<?> output,
            VideoEncoding.VideoSettings video,
            boolean rateControl
    ) {
        output.setVideoCodec(encoder(video.codec()));
        output.setVideoPixelFormat("yuv420p");
        if (video.preset() != null)
            output.setPreset(video.preset().name());
        if (video.frameRate() != null)
            output.setVideoFrameRate(video.frameRate(), 1);
        if (rateControl) {
            if (video.constantRateFactor() != null)
                output.setConstantRateFactor(video.constantRateFactor());
            if (video.videoBitrate() != null)
                output.setVideoBitRate(video.videoBitrate());
        }
        if (Boolean.TRUE.equals(video.allowExperimental()))
            output.setStrict(Strict.EXPERIMENTAL);
    }

    private static void applyAudio(
            AbstractFFmpegOutputBuilder<?> output,
            VideoEncoding.AudioSettings audio,
            boolean rateControl
    ) {
        if (audio.disabled()) {
            output.disableAudio();
            return;
        }
        output.setAudioCodec(encoder(audio.codec()));
        if (rateControl && audio.bitrate() != null)
            output.setAudioBitRate(audio.bitrate());
        if (audio.channels() != null)
            output.setAudioChannels(audio.channels());
        if (audio.sampleRateHz() != null)
            output.setAudioSampleRate(audio.sampleRateHz());
    }

    /**
     * The filter graph that scales every HLS variant independently.
     */
    private static String filterGraph(VideoEncoding.Hls hls) {
        var tracks = hls.tracks();
        var filters = new ArrayList<String>();
        if (tracks.size() > 1) {
            var split = new StringBuilder("[0:v]split=").append(tracks.size());
            for (int i = 0; i < tracks.size(); i++)
                split.append("[v").append(i).append("]");
            filters.add(split.toString());
        }
        for (int i = 0; i < tracks.size(); i++) {
            var track = tracks.get(i);
            var filter = scaleFilter(
                    effectiveLimit(track.limitWidth(), hls.video().limitWidth()),
                    effectiveLimit(track.limitHeight(), hls.video().limitHeight()));
            var input = tracks.size() > 1 ? "[v" + i + "]" : "[0:v]";
            filters.add(input + (filter != null ? filter : "null") + "[v" + i + "out]");
        }
        return String.join(";", filters);
    }

    /**
     * The scale expression keeps the aspect ratio and never upscales. A limit is rounded down to
     * an even number because H.264 with yuv420p cannot encode odd dimensions; ffmpeg's own
     * {@code force_divisible_by} does not adjust an explicitly odd width or height.
     */
    private static @Nullable String scaleFilter(
            @Nullable LimitResolution limitWidth,
            @Nullable LimitResolution limitHeight
    ) {
        var width = limitWidth == null ? null : even(limitWidth.pixels());
        var height = limitHeight == null ? null : even(limitHeight.pixels());
        if (width == null && height == null)
            return null;
        if (width != null && height != null)
            return "scale=w=min(iw\\," + width + "):h=min(ih\\," + height
                    + "):force_original_aspect_ratio=decrease:force_divisible_by=2";
        if (width != null)
            return "scale=w=min(iw\\," + width + "):h=-2";
        return "scale=w=-2:h=min(ih\\," + height + ")";
    }

    private static @Nullable LimitResolution effectiveLimit(
            @Nullable LimitResolution trackLimit,
            @Nullable LimitResolution playlistLimit
    ) {
        return trackLimit != null ? trackLimit : playlistLimit;
    }

    private static int even(int pixels) {
        return Math.max(2, pixels & ~1);
    }

    private static String encoder(VideoEncoding.VideoCodec codec) {
        return switch (codec) {
            case H264 -> "libx264";
        };
    }

    private static String encoder(VideoEncoding.AudioCodec codec) {
        return switch (codec) {
            case AAC -> "aac";
        };
    }
}
