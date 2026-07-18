package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.lazy_transformation_store.LazyTransformationStore;
import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.transformations.sources.TransformationSource;
import com.example.uploads_api.transformations.sources.VideoTransformationSource;
import com.example.uploads_api.uploads.Upload;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TransformationService {
    private final BlockingTransformationService blockingTransformationService;
    private final LazyTransformationService lazyTransformationService;
    private final List<ImageTransformationSource> imageTransformations;
    private final List<VideoTransformationSource> videoTransformations;
    private final LazyTransformationStore lazyTransformationStore;

    /**
     * Completes or queues all matching transformations for the upload.
     * TODO test return
     * @return True if there is at least one lazy transformation in progress.
     */
    public boolean applyTransformations(@NotNull Upload upload) {

        // get which transformations are applicable based on their filters and the upload
        var applicableImageTransformations = filterApplicable(imageTransformations, upload);
        var applicableVideoTransformations = filterApplicable(videoTransformations, upload);

        // group by laziness
        var lazyImageTransformations = filterLazy(applicableImageTransformations);
        var blockingImageTransformations = filterBlocking(applicableImageTransformations);
        var lazyVideoTransformations = filterLazy(applicableVideoTransformations);
        var blockingVideoTransformations = filterBlocking(applicableVideoTransformations);

        // get lazy name list
        var lazyNames = Stream.concat(lazyImageTransformations.stream(), lazyVideoTransformations.stream())
                .map(TransformationSource::getName)
                .toList();

        // collect jobs
        ArrayList<Runnable> jobs = new ArrayList<>();

        if (!blockingImageTransformations.isEmpty())
            jobs.add(() -> blockingTransformationService.transformImages(createTasks(blockingImageTransformations, upload)));
        if (!blockingVideoTransformations.isEmpty())
            jobs.add(() -> blockingTransformationService.transformVideos(createTasks(blockingVideoTransformations, upload)));
        if (!lazyImageTransformations.isEmpty())
            jobs.add(() -> lazyTransformationService.queueImageTransformations(createTasks(lazyImageTransformations, upload)));
        if (!lazyVideoTransformations.isEmpty())
            jobs.add(() -> lazyTransformationService.queueVideoTransformations(createTasks(lazyVideoTransformations, upload)));
        if (!lazyNames.isEmpty())
            jobs.add(() -> lazyTransformationStore.createLazyTransformationSession(upload.id(), lazyNames));

        // execute
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            var futures = jobs.stream()
                    .map(runnable -> CompletableFuture.runAsync(runnable, executor))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
        }

        // return true if awaiting lazy transformations
        return !lazyNames.isEmpty();
    }

    private <DTO, Transformation extends TransformationSource<DTO>> @NonNull List<DTO> createTasks(@NotNull List<Transformation> transformations, @NotNull Upload upload) {
        return transformations.stream()
                .map(transformation -> transformation.createTaskDTO(upload))
                .toList();
    }

    private <Transformation extends TransformationSource<?>> @NonNull List<Transformation> filterApplicable(@NotNull List<Transformation> transformations, @NotNull Upload upload) {
        return transformations.stream()
                .filter(transformation -> transformation.isApplicable(upload))
                .toList();
    }

    private <Transformation extends TransformationSource<?>> @NonNull List<Transformation> filterLazy(@NotNull List<Transformation> transformations) {
        return transformations.stream()
                .filter(Transformation::isLazy)
                .toList();
    }

    private <Transformation extends TransformationSource<?>> @NonNull List<Transformation> filterBlocking(@NotNull List<Transformation> transformations) {
        return transformations.stream()
                .filter(t -> !t.isLazy())
                .toList();
    }
}
