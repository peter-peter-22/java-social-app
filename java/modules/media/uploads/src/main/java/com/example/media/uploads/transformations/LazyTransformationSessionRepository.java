package com.example.media.uploads.transformations;

import com.example.media_api.uploads.UploadId;
import org.springframework.stereotype.Repository;

@Repository
class LazyTransformationSessionRepository {

    /**
     * Creates a new lazy transformation session for the given upload and awaited transformations.
     * @param uploadId The original upload ID.
     * @param requiredTransformations The names of the lazy transformations that need to be completed.
     */
    public void createLazyTransformationSession(UploadId uploadId, Iterable<String> requiredTransformations) {
        System.out.println("Creating lazy transformation session for upload " + uploadId);
        System.out.println("Required transformations: " + String.join(", ", requiredTransformations));
    }

    /**
     * Marks a lazy transformation as ready in the transformation session.
     * @param uploadId The ID of the original upload.
     * @param transformationName The name of the completed transformation.
     * @return When all transformations are ready, the method returns true. Otherwise, it returns false.
     */
    public boolean markLazyTransformationAsReady(UploadId uploadId, String transformationName) {
        System.out.println("Marking lazy transformation " + transformationName + " as ready for upload " + uploadId);
        return false;
    }

    public void deleteLazyTransformationSession(UploadId uploadId) {
        System.out.println("Deleting lazy transformation session for upload " + uploadId);
    }
}
