package com.example.media.video_worker.transform;

import com.example.media.contract.asset.VideoMetadata;
import com.example.media.contract.transformation.VideoTransformationSpec;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.shared.CodecType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The ffmpeg adapter of the video worker. It probes the source, builds the command with
 * {@link FfmpegCommandBuilder}, runs it, and reads the metadata of the result.
 *
 * <p>HLS produces one playlist per variant plus a master playlist. The metadata of an HLS result
 * is the largest variant, because the browser uses it to size the video element; the master playlist
 * probe exposes every variant stream, and {@code readMetadata} selects the largest one.</p>
 */
@Component
public class VideoProcessor implements VideoProcessing {

    private static final int DEFAULT_FRAME_RATE = 30;

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FfmpegCommandBuilder commands = new FfmpegCommandBuilder();

    public VideoProcessor() {
        this(createFfmpeg(), createFfprobe());
    }

    VideoProcessor(FFmpeg ffmpeg, FFprobe ffprobe) {
        this.ffmpeg = Objects.requireNonNull(ffmpeg, "ffmpeg");
        this.ffprobe = Objects.requireNonNull(ffprobe, "ffprobe");
    }

    /**
     * Reads the video metadata with ffprobe and selects the largest video stream of an HLS result.
     */
    @Override
    public VideoMetadata readMetadata(Path file) {
        try {
            return metadataOf(ffprobe.probe(file.toAbsolutePath().toString()), file);
        } catch (IOException e) {
            throw new VideoProcessingException("Failed to extract video metadata: " + file, e);
        }
    }

    /**
     * Creates the output files of a video transformation and returns the entrypoint and metadata.
     */
    @Override
    public ProcessedFile process(Path source, Path outputDir, VideoTransformationSpec spec) {
        try {
            Files.createDirectories(outputDir);
            var sourceProbe = ffprobe.probe(source.toAbsolutePath().toString());
            var sourceVideo = sourceVideo(sourceProbe);
            var hasAudio = sourceProbe.getStreams().stream()
                    .anyMatch(stream -> stream.getCodecType() == CodecType.AUDIO);
            var command = commands.build(
                    source, outputDir, spec, hasAudio,
                    sourceVideo.getWidth(), sourceVideo.getHeight(), frameRate(sourceVideo));
            new FFmpegExecutor(ffmpeg, ffprobe).createJob(command.builder()).run();
            return new ProcessedFile(command.entryPoint(), readMetadata(command.entryPoint()));
        } catch (IOException e) {
            throw new VideoProcessingException("Failed to encode the video: " + source, e);
        } catch (RuntimeException e) {
            if (e instanceof VideoProcessingException)
                throw e;
            throw new VideoProcessingException("Failed to encode the video: " + source, e);
        }
    }

    private static VideoMetadata metadataOf(FFmpegProbeResult probe, Path file) {
        var videos = probe.getStreams().stream()
                .filter(stream -> stream.getCodecType() == CodecType.VIDEO)
                .toList();
        if (videos.isEmpty())
            throw new VideoProcessingException("No video stream found in: " + file);

        var largest = videos.stream()
                .max(Comparator.comparingLong(stream -> (long) stream.getWidth() * stream.getHeight()))
                .orElseThrow();
        if (largest.getWidth() <= 0 || largest.getHeight() <= 0)
            throw new VideoProcessingException("The video stream has no dimensions: " + file);

        var durationSeconds = durationSeconds(probe, videos);
        if (durationSeconds <= 0)
            throw new VideoProcessingException("The video duration could not be read: " + file);
        return new VideoMetadata(
                largest.getWidth(),
                largest.getHeight(),
                Math.max(1, Math.round(durationSeconds * 1000)));
    }

    /**
     * The format duration is the reliable one; streams are the fallback for some containers.
     */
    private static double durationSeconds(FFmpegProbeResult probe, List<FFmpegStream> videos) {
        FFmpegFormat format = probe.getFormat();
        if (format != null && format.getDuration() > 0)
            return format.getDuration();
        var videoDuration = videos.stream().mapToDouble(FFmpegStream::getDuration).max().orElse(0);
        if (videoDuration > 0)
            return videoDuration;
        return probe.getStreams().stream().mapToDouble(FFmpegStream::getDuration).max().orElse(0);
    }

    /**
     * The source video stream supplies the dimensions and the GOP frame rate.
     */
    private static FFmpegStream sourceVideo(FFmpegProbeResult probe) {
        var video = probe.getStreams().stream()
                .filter(stream -> stream.getCodecType() == CodecType.VIDEO)
                .findFirst()
                .orElseThrow(() -> new VideoProcessingException("No video stream found in the source"));
        if (video.getWidth() <= 0 || video.getHeight() <= 0)
            throw new VideoProcessingException("The source video stream has no dimensions");
        return video;
    }

    /**
     * The source frame rate feeds the HLS GOP when the transformation does not set one.
     */
    private static int frameRate(FFmpegStream video) {
        var rate = video.getAvgFrameRate();
        if (rate == null || rate.getNumerator() == 0)
            rate = video.getRFrameRate();
        if (rate == null || rate.getNumerator() == 0 || rate.getDenominator() == 0)
            return DEFAULT_FRAME_RATE;
        var value = rate.doubleValue();
        return value > 0 ? Math.max(1, (int) Math.round(value)) : DEFAULT_FRAME_RATE;
    }

    private static FFmpeg createFfmpeg() {
        try {
            return new FFmpeg();
        } catch (IOException e) {
            throw new IllegalStateException("ffmpeg is not available", e);
        }
    }

    private static FFprobe createFfprobe() {
        try {
            return new FFprobe();
        } catch (IOException e) {
            throw new IllegalStateException("ffprobe is not available", e);
        }
    }
}
