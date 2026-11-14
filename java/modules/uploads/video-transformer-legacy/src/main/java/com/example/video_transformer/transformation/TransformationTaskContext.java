package com.example.video_transformer.transformation;

import com.example.uploads_api.transformations.tasks.VideoTransformationTaskGroup;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

record TransformationTaskContext(
        VideoTransformationTaskGroup.@NonNull VideoTask task,
        @NonNull VideoTransformationTaskGroup group,
        @NonNull FFmpegProbeResult input,
        @NonNull Path taskOutputDir,
        @NonNull FFmpegBuilder builder
) {
}
