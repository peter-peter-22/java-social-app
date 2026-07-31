package com.example.uploads_api.transformations.tasks;

import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public interface TransformationTaskGroup {
    @NonNull ObjectLocation inputObject();

    // "? extends TransformationTask" is needed because subclasses don't work with generics (Collection<T> is the generic here)
    @NonNull Collection<? extends TransformationTask> tasks();

    @NonNull UploadId uploadId();
}
