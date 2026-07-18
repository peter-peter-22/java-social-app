package com.example.uploads_api.transformations.lazy_transformation_store;

import com.example.uploads_api.uploads.UploadId;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface LazyTransformationStore {
    /**
     * Marks a pending lazy transformation as ready and deletes the session if no more lazy transformations are queued.
     */
    void markLazyTransformationAsComplete(@NotNull UploadId uploadId, @NotNull String transformationName);

    /**
     * Creates a new lazy transformation session for the given upload and awaited lazy transformations.
     */
    void createLazyTransformationSession(@NotNull UploadId uploadId, @NotNull Collection<@NotNull String> requiredTransformations);
}
