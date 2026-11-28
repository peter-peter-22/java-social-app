package com.example.media.video_worker;

import com.example.media.contract.transformation.LimitResolution;
import com.example.media.contract.transformation.VideoEncoding;
import com.example.media.contract.transformation.VideoTransformationSpec;
import com.example.media.video_worker.transform.VideoProcessor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.shared.CodecType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The video transformation output lock: the real ffmpeg adapter against the checked-in fixture.
 * Assertions are structural (dimensions, codecs, stream presence, playlist structure, relative
 * file sizes), never byte-exact, because the encoder output drifts with the ffmpeg version. Every
 * output is copied to {@code build/reports/transformations/} for visual inspection.
 *
 * <p>The suite needs ffmpeg on the PATH. Run it through
 * {@code deployment/test-video-transformations.bash} in the worker image, or locally where ffmpeg
 * is installed.</p>
 */
class VideoProcessorTest {

    private static final FFprobe FFPROBE = createFfprobe();

    private final VideoProcessor processor = new VideoProcessor();
    private final Path reportDir = Path.of("build", "reports", "transformations");

    @TempDir
    Path tempDir;

    @Test
    void readMetadataReportsTheSourceDimensionsAndDuration() throws IOException {
        var metadata = processor.readMetadata(source());

        assertThat(metadata.width()).isEqualTo(320);
        assertThat(metadata.height()).isEqualTo(240);
        assertThat(metadata.durationMs()).isBetween(9_000L, 11_000L);
    }

    @Test
    void mp4OutputKeepsTheSourceDimensionsAndHasAudio() throws IOException {
        var out = out("mp4-default");

        var processed = processor.process(source(), out, mp4(
                video().preset(VideoEncoding.VideoEncodingPreset.veryfast).build(),
                VideoEncoding.AudioSettings.aac()));

        saveReport("mp4 default", out);
        assertThat(processed.entryPoint()).isEqualTo(out.resolve("result.mp4"));
        assertThat(processed.metadata().width()).isEqualTo(320);
        assertThat(processed.metadata().height()).isEqualTo(240);

        var videos = streams(processed.entryPoint(), CodecType.VIDEO);
        assertThat(videos).hasSize(1);
        assertThat(videos.getFirst().getCodecName()).isEqualTo("h264");
        assertThat(videos.getFirst().getPixFmt()).isEqualTo("yuv420p");
        assertThat(streams(processed.entryPoint(), CodecType.AUDIO)).isNotEmpty();
    }

    @Test
    void mp4ResolutionLimitsKeepTheAspectRatio() throws IOException {
        var source = source();

        assertDimensions(source, "limit-width-",
                video().limitWidth(limit(200)).build(), 200, 150);
        assertDimensions(source, "limit-height-",
                video().limitHeight(limit(100)).build(), 134, 100);
        assertDimensions(source, "limit-both-",
                video().limitWidth(limit(200)).limitHeight(limit(100)).build(), 134, 100);
        assertDimensions(source, "limit-no-upscale-",
                video().limitWidth(limit(1000)).limitHeight(limit(1000)).build(), 320, 240);
        assertDimensions(source, "limit-odd-width-",
                video().limitWidth(limit(201)).build(), 200, 150);
    }

    @Test
    void mp4CrfChangesTheFileSize() throws IOException {
        var source = source();
        var lowerQuality = processor.process(source, out("crf-high"), mp4(
                video().constantRateFactor(50).build(), VideoEncoding.AudioSettings.aac()));
        var higherQuality = processor.process(source, out("crf-low"), mp4(
                video().constantRateFactor(15).build(), VideoEncoding.AudioSettings.aac()));

        saveReport("mp4 crf 50", lowerQuality.entryPoint().getParent());
        saveReport("mp4 crf 15", higherQuality.entryPoint().getParent());
        assertThat(Files.size(higherQuality.entryPoint())).isGreaterThan(Files.size(lowerQuality.entryPoint()));
    }

    @Test
    void mp4FrameRateIsApplied() throws IOException {
        var processed = processor.process(source(), out("frame-rate"), mp4(
                video().frameRate(5).build(), VideoEncoding.AudioSettings.aac()));

        saveReport("mp4 frame rate", processed.entryPoint().getParent());
        var video = streams(processed.entryPoint(), CodecType.VIDEO).getFirst();
        assertThat(video.getAvgFrameRate().doubleValue()).isBetween(4.5, 5.5);
    }

    @Test
    void mp4CanDisableAudio() throws IOException {
        var out = out("no-audio");

        var processed = processor.process(source(), out, mp4(
                video().build(), VideoEncoding.AudioSettings.noAudio()));

        saveReport("mp4 no audio", out);
        assertThat(streams(processed.entryPoint(), CodecType.AUDIO)).isEmpty();
    }

    private void assertDimensions(
            Path source,
            String reportName,
            VideoEncoding.VideoSettings video,
            int expectedWidth,
            int expectedHeight
    ) throws IOException {
        var out = out(reportName);
        var processed = processor.process(source, out, mp4(video, VideoEncoding.AudioSettings.aac()));
        saveReport("mp4 " + reportName, out);
        assertThat(processed.metadata().width()).isEqualTo(expectedWidth);
        assertThat(processed.metadata().height()).isEqualTo(expectedHeight);
    }

    private static VideoEncoding.VideoSettings.VideoSettingsBuilder video() {
        return VideoEncoding.VideoSettings.builder()
                .codec(VideoEncoding.VideoCodec.H264)
                .preset(VideoEncoding.VideoEncodingPreset.veryfast);
    }

    private static LimitResolution limit(int pixels) {
        return new LimitResolution(pixels, LimitResolution.Mode.KEEP_ASPECT_RATIO);
    }

    private static VideoTransformationSpec mp4(
            VideoEncoding.VideoSettings video,
            VideoEncoding.AudioSettings audio
    ) {
        return new VideoTransformationSpec(VideoEncoding.Mp4.builder()
                .video(video)
                .audio(audio)
                .build());
    }

    @Test
    void hlsOutputsATwoTrackLadderWithTheLargestVariantAsMetadata() throws IOException {
        var out = out("hls-ladder");
        var spec = new VideoTransformationSpec(VideoEncoding.Hls.builder()
                .video(video().frameRate(30).constantRateFactor(23).build())
                .audio(VideoEncoding.AudioSettings.builder()
                        .codec(VideoEncoding.AudioCodec.AAC)
                        .bitrate(96_000)
                        .build())
                .segmentSeconds(2)
                .tracks(List.of(
                        VideoEncoding.Hls.HlsTrack.builder()
                                .name("high")
                                .limitWidth(limit(200))
                                .videoBitrate(800_000)
                                .constantRateFactor(20)
                                .audioBitrate(128_000)
                                .build(),
                        VideoEncoding.Hls.HlsTrack.builder()
                                .name("low")
                                .limitWidth(limit(100))
                                .videoBitrate(200_000)
                                .constantRateFactor(30)
                                .audioBitrate(96_000)
                                .build()))
                .build());

        var processed = processor.process(source(), out, spec);

        saveReport("hls ladder", out);
        assertThat(processed.entryPoint()).isEqualTo(out.resolve("master.m3u8"));
        var playlist = Files.readString(processed.entryPoint());
        assertThat(playlist.lines().filter(line -> line.startsWith("#EXT-X-STREAM-INF")).count()).isEqualTo(2);
        assertThat(playlist).contains("RESOLUTION=200x150").contains("RESOLUTION=100x76");

        var bandwidths = playlist.lines()
                .filter(line -> line.startsWith("#EXT-X-STREAM-INF"))
                .map(line -> line.substring(line.indexOf("BANDWIDTH=") + "BANDWIDTH=".length(),
                        line.indexOf(',', line.indexOf("BANDWIDTH="))))
                .toList();
        assertThat(bandwidths).hasSize(2).doesNotHaveDuplicates();

        assertThat(processed.metadata().width()).isEqualTo(200);
        assertThat(processed.metadata().height()).isEqualTo(150);
        assertThat(out.resolve("var-high/index.m3u8")).exists();
        assertThat(out.resolve("var-low/index.m3u8")).exists();
        assertThat(out.resolve("var-high/segment-0.m4s")).exists();
        assertThat(Files.readString(out.resolve("var-high/index.m3u8")))
                .contains("#EXT-X-PLAYLIST-TYPE:VOD")
                .contains("#EXT-X-MAP");

        var high = videoStream(out.resolve("var-high/index.m3u8"));
        assertThat(high.getWidth()).isEqualTo(200);
        assertThat(high.getHeight()).isEqualTo(150);
        var low = videoStream(out.resolve("var-low/index.m3u8"));
        assertThat(low.getWidth()).isEqualTo(100);
        assertThat(low.getHeight()).isEqualTo(76);
    }

    @Test
    void hlsWorksWithoutAudio() throws Exception {
        var out = out("hls-no-audio");
        var spec = new VideoTransformationSpec(VideoEncoding.Hls.builder()
                .video(video().frameRate(30).build())
                .audio(VideoEncoding.AudioSettings.aac())
                .segmentSeconds(2)
                .tracks(List.of(VideoEncoding.Hls.HlsTrack.builder()
                        .name("single")
                        .limitWidth(limit(200))
                        .videoBitrate(500_000)
                        .build()))
                .build());

        var processed = processor.process(silentSource(), out, spec);

        saveReport("hls no audio", out);
        assertThat(Files.readString(processed.entryPoint())).contains("#EXT-X-STREAM-INF");
        assertThat(out.resolve("var-single/index.m3u8")).exists();
        assertThat(streams(out.resolve("var-single/index.m3u8"), CodecType.AUDIO)).isEmpty();
        var video = videoStream(out.resolve("var-single/index.m3u8"));
        assertThat(video.getWidth()).isEqualTo(200);
        assertThat(video.getHeight()).isEqualTo(150);
        assertThat(processed.metadata().width()).isEqualTo(200);
        assertThat(processed.metadata().height()).isEqualTo(150);
    }

    private Path source() throws IOException {
        var file = tempDir.resolve("source.mp4");
        if (!Files.exists(file)) {
            try (var input = new ClassPathResource("video.mp4").getInputStream()) {
                Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return file;
    }

    private Path silentSource() throws IOException, InterruptedException {
        var source = source();
        var file = tempDir.resolve("silent.mp4");
        var process = new ProcessBuilder(
                "ffmpeg", "-hide_banner", "-loglevel", "error", "-y",
                "-i", source.toString(), "-an", "-c:v", "copy", file.toString())
                .redirectErrorStream(true)
                .start();
        var output = new String(process.getInputStream().readAllBytes());
        if (process.waitFor() != 0)
            throw new IOException("ffmpeg failed to create the silent fixture: " + output);
        return file;
    }

    private Path out(String name) throws IOException {
        var directory = tempDir.resolve("out-" + name);
        Files.createDirectories(directory);
        return directory;
    }

    private static List<FFmpegStream> streams(Path file, CodecType type) {
        return probe(file).getStreams().stream()
                .filter(stream -> stream.getCodecType() == type)
                .toList();
    }

    private static FFmpegStream videoStream(Path file) {
        var videos = streams(file, CodecType.VIDEO);
        assertThat(videos).isNotEmpty();
        return videos.getFirst();
    }

    private static FFmpegProbeResult probe(Path file) {
        try {
            return FFPROBE.probe(file.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new UncheckedIOException("could not probe " + file, e);
        }
    }

    private void saveReport(String name, Path outputDir) {
        try (Stream<Path> paths = Files.walk(outputDir)) {
            for (var path : paths.toList()) {
                var relative = outputDir.relativize(path);
                var target = relative.toString().isEmpty()
                        ? reportDir.resolve(name)
                        : reportDir.resolve(name).resolve(relative);
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("could not save the transformation report", e);
        }
    }

    private static FFprobe createFfprobe() {
        try {
            return new FFprobe();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
