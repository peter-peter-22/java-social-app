package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.dto.VideoTransformationTaskDTO;
import com.example.uploads_api.transformations.lazy_transformation_store.LazyTransformationStore;
import com.example.uploads_api.transformations.mappers.ImageTransformationSourceMapper;
import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.transformations.sources.TransformationSource;
import com.example.uploads_api.transformations.sources.VideoTransformationSource;
import com.example.uploads_api.uploads.Upload;
import lombok.RequiredArgsConstructor;
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
     * @return True if there is at least one lazy transformation in progress.
     */
    public boolean applyTransformations(@NonNull Upload upload) {

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
            jobs.add(() -> blockingTransformationService.transformImages(createImageTasks(blockingImageTransformations, upload)));
        if (!blockingVideoTransformations.isEmpty())
            jobs.add(() -> blockingTransformationService.transformVideos(createVideoTasks(blockingVideoTransformations, upload)));
        if (!lazyImageTransformations.isEmpty())
            jobs.add(() -> lazyTransformationService.queueImageTransformations(createImageTasks(lazyImageTransformations, upload)));
        if (!lazyVideoTransformations.isEmpty())
            jobs.add(() -> lazyTransformationService.queueVideoTransformations(createVideoTasks(lazyVideoTransformations, upload)));
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

    // CLEAN: can be abstracted?
    private @NonNull ImageTransformationTaskGroupDTO createImageTasks(
            @NonNull List<ImageTransformationSource> transformations,
            @NonNull Upload upload
    ) {
        var tasks = transformations.stream()
                .map(transformation -> ImageTransformationSourceMapper.createTaskDTO(transformation, upload))
                .toList();
        return new ImageTransformationTaskGroupDTO(upload.objectLocation(), tasks);
    }

    private @NonNull List<VideoTransformationTaskDTO> createVideoTasks(
            @NonNull List<VideoTransformationSource> transformations,
            @NonNull Upload upload
    ) {
        return List.of(); // placeholder
    }

    private <Transformation extends TransformationSource> @NonNull List<Transformation> filterApplicable(@NonNull List<Transformation> transformations, @NonNull Upload upload) {
        return transformations.stream()
                .filter(transformation -> transformation.isApplicable(upload))
                .toList();
    }

    private <Transformation extends TransformationSource> @NonNull List<Transformation> filterLazy(@NonNull List<Transformation> transformations) {
        return transformations.stream()
                .filter(Transformation::isLazy)
                .toList();
    }

    private <Transformation extends TransformationSource> @NonNull List<Transformation> filterBlocking(@NonNull List<Transformation> transformations) {
        return transformations.stream()
                .filter(t -> !t.isLazy())
                .toList();
    }
}
