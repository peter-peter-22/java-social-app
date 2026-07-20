package com.example.uploads_api.transformations.lazy_transformation_store;

import com.example.uploads_api.uploads.UploadId;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public interface LazyTransformationStore {
    /**
     * Marks a pending lazy transformation as ready and deletes the session if no more lazy transformations are queued.
     */
    void markLazyTransformationAsComplete(@NonNull UploadId uploadId, @NonNull String transformationName);

    /**
     * Creates a new lazy transformation session for the given upload and awaited lazy transformations.
     */
    void createLazyTransformationSession(@NonNull UploadId uploadId, @NonNull Collection<@NonNull String> requiredTransformations);

    /**
     * Returns true if all lazy transformations are ready
     */
    boolean checkIfReady(@NonNull UploadId uploadId);
}
