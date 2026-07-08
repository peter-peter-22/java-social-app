package com.example.uploads.transformations;

import com.example.media_api.transformations.UploadTransformation;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.uploads.Upload;
import com.example.media_api.uploads.UploadId;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class TransformationService {
    private final LazyTransformationSessionRepository lazyTransformationSessionRepository;
    private final LazyTransformationApi lazyTransformationApi;
    private final BlockingTransformationApi blockingTransformationApi;
    private final List<UploadTransformation> transformations;

    /**
     * Completes or queues all matching transformations for the upload.
     *
     * @return True if there is at least one lazy transformation in progress.
     */
    public boolean applyTransformations(@NotNull Upload upload) {
        // get which transformations are applicable based on their filters and the upload
        var applicableTransformations = transformations.stream()
                .filter(transformation -> transformation.isApplicable(upload))
                .toList();

        // execute
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {

            var blockingFuture = CompletableFuture.runAsync(
                    () -> applyBlockingTransformations(applicableTransformations, upload.id()),
                    executor
            );

            var lazyFuture = CompletableFuture.supplyAsync(
                    () -> applyLazyTransformations(applicableTransformations, upload.id()),
                    executor
            );

            CompletableFuture.allOf(blockingFuture, lazyFuture).join();

            return lazyFuture.join();
        }
    }

    private static void throwUnchecked(Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException) throw runtimeException;
        if (throwable instanceof Error error) throw error;
        throw new IllegalStateException("Failed to apply transformations", throwable);
    }

    private void applyBlockingTransformations(@NotNull Collection<UploadTransformation> applicableTransformations, UploadId uploadId) {
        var blockingTransformations = applicableTransformations.stream()
                .filter(transformation -> !transformation.isLazy())
                .map(transformation -> UploadTransformationDTO.toDTO(transformation, uploadId))
                .toList();

        if (blockingTransformations.isEmpty()) return;

        blockingTransformationApi.transform(blockingTransformations);
    }

    /**
     * Creates lazy transformation session and events if necessary.
     * Optimization: lazyTransformationSessionRepository.createLazyTransformationSession and lazyTransformationApi.transform
     * could be executed in parallel.
     *
     * @return True if there is at least one lazy transformation.
     */
    private boolean applyLazyTransformations(@NotNull Collection<UploadTransformation> applicableTransformations, UploadId uploadId) {
        // get the list of the lazy transformations
        var lazyTransformations = applicableTransformations.stream()
                .filter(UploadTransformation::isLazy)
                .map(transformation -> UploadTransformationDTO.toDTO(transformation, uploadId))
                .toList();

        // if no lazy transformations found, exit
        if (lazyTransformations.isEmpty()) return false;

        // if lazy transformations are required, create the lazy transformation session and events
        var names = lazyTransformations.stream()
                .map(UploadTransformationDTO::name)
                .toList();
        lazyTransformationSessionRepository.createLazyTransformationSession(uploadId, names);
        lazyTransformationApi.transform(lazyTransformations);

        return true;
    }

    /**
     * Marks a lazy transformation as ready and deletes the session if no more transformations are required.
     */
    public void markLazyTransformationAsComplete(UploadId uploadId, String transformationName) {
        boolean ready = lazyTransformationSessionRepository.markLazyTransformationAsReady(uploadId, transformationName);
        if (ready)
            lazyTransformationSessionRepository.deleteLazyTransformationSession(uploadId);
    }
}
