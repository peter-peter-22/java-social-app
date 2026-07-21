package com.example.image_transformer.task;

import com.example.uploads_api.uploads.ObjectLocation;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public record ImageTransformationTaskGroup(
        @NonNull ObjectLocation inputObject,
        @NonNull Collection<ImageTransformationTask> tasks
) {
}
