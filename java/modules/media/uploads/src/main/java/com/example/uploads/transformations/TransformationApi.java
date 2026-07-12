package com.example.uploads.transformations;

import com.example.media_api.transformations.task.UploadTransformationTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

interface TransformationApi {
    void transform(@NotNull Collection<UploadTransformationTask> transformations);
}
