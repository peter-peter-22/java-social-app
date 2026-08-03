package com.example.uploads_api.transformations.tasks;

import com.example.uploads_api.v2.uploads.upload_registry.Upload;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public interface TransformationTaskGroup {
    @NonNull
    Upload original();

    @NonNull Collection<? extends @NonNull TransformationTask> tasks();

    boolean async();

    String completedNotificationUrl();

    String progressNotificationUrl();
}
