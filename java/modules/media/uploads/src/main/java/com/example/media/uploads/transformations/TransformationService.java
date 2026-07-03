package com.example.media.uploads.transformations;

import com.example.media.common.transformations.UploadTransformation;
import com.example.media.common.uploads.Upload;
import com.example.media.common.uploads.UploadId;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransformationService {
    private final LazyTransformationSessionRepository lazyTransformationSessionRepository;
    private final LazyTransformationEventRepository lazyTransformationEventRepository;
    private final BlockingTransformationRepository blockingTransformationRepository;
    private final List<UploadTransformation> transformations;

    /**
     * Completes or queues all matching transformations for the upload.
     * Optimization: the lazy and the blocking transformations could be applied in parallel.
     *
     * @return True if there is at least one lazy transformation in progress.
     */
    public boolean applyTransformations(@NotNull Upload upload) {
        // get which transformations are applicable based on their filters and the upload
        var applicableTransformations = transformations.stream()
                .filter(transformation -> transformation.isApplicable(upload))
                .toList();

        // wait for blocking transformations to complete
        applyBlockingTransformations(applicableTransformations, upload.id());

        // queue lazy transformations if any, return true if there is at least one
        return applyLazyTransformations(applicableTransformations, upload.id());
    }

    private void applyBlockingTransformations(@NotNull Collection<UploadTransformation> applicableTransformations, UploadId uploadId) {
        var blockingTransformations = applicableTransformations.stream()
                .filter(transformation -> !transformation.isLazy())
                .toList();

        blockingTransformationRepository.applyTransformations(uploadId, blockingTransformations);
    }

    /**
     * Creates lazy transformation session and events if necessary.
     *
     * @return True if there is at least one lazy transformation.
     */
    private boolean applyLazyTransformations(@NotNull Collection<UploadTransformation> applicableTransformations, UploadId uploadId) {
        // get the list of the lazy transformations
        var lazyTransformations = applicableTransformations.stream()
                .filter(UploadTransformation::isLazy)
                .toList();

        // if no lazy transformations found, exit
        if (lazyTransformations.isEmpty()) return false;

        // if lazy transformations are required, create the lazy transformation session and events
        var names = lazyTransformations.stream()
                .map(UploadTransformation::getName)
                .toList();
        lazyTransformationSessionRepository.createLazyTransformationSession(uploadId, names);
        lazyTransformationEventRepository.queueLazyUploadTransformations(uploadId, applicableTransformations);

        return true;
    }

    /** Marks a lazy transformation as ready and deletes the session if no more transformations are required. */
    public void markLazyTransformationAsComplete(UploadId uploadId, String transformationName)
    {
        boolean ready = lazyTransformationSessionRepository.markLazyTransformationAsReady(uploadId, transformationName);
        if (ready)
            lazyTransformationSessionRepository.deleteLazyTransformationSession(uploadId);
    }
}
