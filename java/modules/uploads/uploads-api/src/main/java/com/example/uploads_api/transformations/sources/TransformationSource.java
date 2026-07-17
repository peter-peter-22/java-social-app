package com.example.uploads_api.transformations.sources;

import com.example.uploads_api.transformations.operations.TransformationOperations;
import com.example.uploads_api.uploads.Upload;
import org.jetbrains.annotations.NotNull;

// CLEAN: should I use only the abstract class instead of the interface?
public interface TransformationSource<T> {
    @NotNull TransformationOperations getOperations();

    @NotNull String getName();

    @NotNull String getOutputBucket();

    boolean isLazy();

    boolean isApplicable(@NotNull Upload upload);

    @NotNull T createTaskDTO(@NotNull Upload original);
}
