package com.example.transformer_contracts;

import com.example.uploads_api.transformations.tasks.TransformationTask;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.function.Consumer;

@Builder
public record FileTransformationArgs(
        @NonNull Path input,
        TransformationTask task,
        Path outputDir,
        Consumer<Integer> onProgress
) {
}
