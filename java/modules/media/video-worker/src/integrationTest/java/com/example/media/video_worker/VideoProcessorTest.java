package com.example.media.video_worker;

import com.example.media.contract.transformation.VideoEncoding;
import com.example.media.contract.transformation.VideoTransformationSpec;
import com.example.media.video_worker.transform.VideoProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VideoProcessorTest {
    private final VideoProcessor processor = new VideoProcessor();

    private final Path reportDir = Path.of("build", "reports", "transformations");
    private final Path inputPath = new ClassPathResource("video.mp4").getFilePath();

    public VideoProcessorTest() throws IOException {
    }

    @Test
    void frameRate() {
        processor.process(
                inputPath,
                getReportDir("frame rate"),
                new VideoTransformationSpec(VideoEncoding.Mp4.builder().videoFrameRate(5).build())
        );
    }

    @Test
    void constantRateFactor() {
        processor.process(
                inputPath,
                getReportDir("constant rate factor"),
                new VideoTransformationSpec(VideoEncoding.Mp4.builder().constantRateFactor(40).build())
        );
    }

    @Test
    void videoCodecs() {
        for (VideoEncoding.VideoCodec codec : VideoEncoding.VideoCodec.values()) {
            processor.process(
                    inputPath,
                    getReportDir("video codec/" + codec.name()),
                    new VideoTransformationSpec(VideoEncoding.Mp4.builder().videoCodec(codec).build())
            );
        }
    }

    @Test
    void encodingPreset() {
        for(VideoEncoding.VideoEncodingPreset preset : VideoEncoding.VideoEncodingPreset.values()) {
            processor.process(
                    inputPath,
                    getReportDir("encoding preset/" + preset.name()),
                    new VideoTransformationSpec(VideoEncoding.Mp4.builder().encodingPreset(preset).build())
            );
        }
    }

    /**
     * Get the directory for the reported outputs of a test.
     */
    private Path getReportDir(String reportName) {
        var path = reportDir.resolve(reportName);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return path;
    }
}
