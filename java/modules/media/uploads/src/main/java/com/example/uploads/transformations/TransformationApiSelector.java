package com.example.uploads.transformations;

import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.media_api.uploads.MediaType;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TransformationApiSelector implements TransformationApi {
    abstract @NonNull TransformationApi getVideoApi();
    abstract @NonNull TransformationApi getImageApi();

    @Override
    public void transform(@NonNull Collection<UploadTransformationTask> transformations) {
        var images = transformations.stream()
                .filter(t -> t.getMediaType().equals(MediaType.IMAGE))
                .toList();

        var videos = transformations.stream()
                .filter(t -> t.getMediaType().equals(MediaType.VIDEO))
                .toList();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            var futures = new ArrayList<CompletableFuture<Void>>();
            if (!images.isEmpty())
                futures.add(CompletableFuture.runAsync(() -> getImageApi().transform(images), executor));
            if (!videos.isEmpty())
                futures.add(CompletableFuture.runAsync(() -> getVideoApi().transform(videos), executor));
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }
    }
}
