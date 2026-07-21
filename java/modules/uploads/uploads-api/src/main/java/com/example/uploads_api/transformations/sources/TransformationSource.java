package com.example.uploads_api.transformations.sources;

import com.example.uploads_api.transformations.operations.TransformationOperations;
import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NonNull;

public interface TransformationSource {
    @NonNull TransformationOperations getOperations();

    @NonNull String getName();

    @NonNull String getOutputBucket();

    boolean isLazy();

    boolean isApplicable(@NonNull Upload upload);

}
