package com.example.uploads.transformations;

import com.example.media_api.transformations.task.UploadTransformationTask;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
class LazyTransformationApi implements TransformationApi {
    /**
     * Queue the given transformations for the given upload with a kafka event.
     */
    @Override
    public void transform(@NonNull Collection<UploadTransformationTask> transformations) {
        System.out.println("Events: " + transformations);
    }
}
