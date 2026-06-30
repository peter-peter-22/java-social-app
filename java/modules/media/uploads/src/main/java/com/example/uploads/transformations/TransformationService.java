package com.example.uploads.transformations;

import com.example.uploads.Upload;

public interface TransformationService {
    /**
     * Completes all matching transformations for the upload.
     * @return True if all matching transformations are synchronous, false if at least one is lazy and the transformations are not ready yet.
     * */
    boolean applyTransformations(Upload upload);
}
