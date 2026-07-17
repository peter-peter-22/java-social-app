package com.example.uploads_persistence.lazy_transformation_session_service;

import com.example.uploads_api.uploads.UploadId;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class LazyTransformationSessionService {
    private final LazyTransformationSessionRepository lazyTransformationSessionRepository;

    /**
     * Marks a lazy transformation as ready and deletes the session if no more transformations are required.
     */
    public void markLazyTransformationAsComplete(@NotNull UploadId uploadId, @NotNull String transformationName) {
        boolean ready = lazyTransformationSessionRepository.markLazyTransformationAsReady(uploadId, transformationName);
        if (ready)
            lazyTransformationSessionRepository.deleteLazyTransformationSession(uploadId);
    }

    /**
     * Creates a new lazy transformation session for the given upload and awaited transformations.
     * Exits without acting if the list is empty.
     */
    public void createLazyTransformationSession(@NotNull UploadId uploadId, @NotNull Collection<String> requiredTransformations) {
        if (requiredTransformations.isEmpty())
            return;
        lazyTransformationSessionRepository.createLazyTransformationSession(uploadId, requiredTransformations);
    }
}
