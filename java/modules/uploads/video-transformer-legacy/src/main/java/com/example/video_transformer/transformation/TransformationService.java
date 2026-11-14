package com.example.video_transformer.transformation;

import com.example.uploads_api.transformations.tasks.VideoTransformationTaskGroup;
import com.example.uploads_api.v2.transformations.operations.VideoEncodings;
import com.example.uploads_api.v2.transformations.operations.VideoTransformationOperations;
import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class TransformationService {
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

    {
        try {
            ffmpeg = new FFmpeg();
            ffprobe = new FFprobe();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize FFMPEG", e);
        }
    }

    public void transformAll(Path inputPath, Path outputDir, VideoTransformationOperations operations) {

        // get metadata
        FFmpegProbeResult in = ffprobe.probe(inputPath.toString());

        // complete the transformation tasks in parallel
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            var futures = group.tasks().stream()
                    .map(task -> CompletableFuture.runAsync(() -> transformSingle(task, group, in), executor))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
        }
    }

    private void transformSingle(VideoTransformationTaskGroup.@NonNull VideoTask task, @NonNull VideoTransformationTaskGroup group, @NonNull FFmpegProbeResult input) {
        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true) // Overwriting is enabled to make the process idempotent
                .setInput(input)
                .done();

        var taskContext = new TransformationTaskContext(
                task,
                group,
                input,
                outputDir.resolve(group.inputObject().bucket(), group.inputObject().key(), task.name()),
                builder
        );

        switch (task.operations().encoding()) {
            case VideoEncodings.Mp4 encoding -> encodeMp4(taskContext, encoding);
            case VideoEncodings.Hls encoding -> encodeHls(taskContext, encoding);
        };

        executor.createJob(builder).run();



    }

    private void encodeMp4(@NonNull TransformationTaskContext context, VideoEncodings.@NonNull Mp4 encoding) {
        var outputFile = context.taskOutputDir().resolve("result.mp4");
        var builder = context.builder().addOutput(outputFile);

        if (encoding.videoBitrate() != null)
            builder.setVideoBitRate(encoding.videoBitrate());
        if (encoding.audioBitrate() != null)
            builder.setAudioBitRate(encoding.audioBitrate());
        if (encoding.videoFrameRate() != null)
            builder.setVideoFrameRate(encoding.videoFrameRate());
        if (encoding.constantRateFactor() != null)
            builder.setConstantRateFactor(encoding.constantRateFactor());
        builder.setPreset(encoding.encodingPreset().name());
    }

    private void encodeHls(@NonNull TransformationTaskContext context, VideoEncodings.@NonNull Hls encoding) {

    }
}
