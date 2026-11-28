package com.example.media.video_worker.transform;

import com.example.media.contract.asset.VideoMetadata;
import com.example.media.contract.transformation.VideoTransformationSpec;

import java.nio.file.Path;

/**
 * The video processing port of the worker: read the dimensions of a file and apply one resolved
 * transformation to it. The production implementation is the ffmpeg adapter ({@link VideoProcessor}).
 */
public interface VideoProcessing {

    /**
     * The encoded result of one transformation: the entry file and the metadata of the video.
     */
    record ProcessedFile(Path entryPoint, VideoMetadata metadata) {
    }

    /**
     * Reads the dimensions of a video file.
     */
    VideoMetadata readMetadata(Path file);

    /**
     * Applies the resolved operations to the source video and writes the encoded result into the
     * output directory. The aspect ratio is applied before the resolution limit.
     */
    ProcessedFile process(Path source, Path outputDir, VideoTransformationSpec spec);
}