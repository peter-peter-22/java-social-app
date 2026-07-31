package com.example.uploads_api.transformations.tasks;

import com.example.uploads_api.uploads.ObjectLocation;
import org.jspecify.annotations.NonNull;

public interface TransformationTask {
    @NonNull ObjectLocation outputObject();

    @NonNull String name();

    boolean lazy();
}
