package com.example.uploads.transformations;

import com.example.media_api.transformations.task.UploadTransformationTask;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility for applying transformation calls in parallel.
 */
abstract class ParallelTransformationApi implements TransformationApi {
    public abstract void call(@NotNull UploadTransformationTask body);

    @Override
    public void transform(@NonNull Collection<UploadTransformationTask> transformations) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            var futures = transformations.stream()
                    .map(transformation -> CompletableFuture.runAsync(() -> call(transformation), executor))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
        }
    }
}
