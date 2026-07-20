package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import org.jspecify.annotations.NonNull;

public interface TransformationOperations {
    @NonNull FileType getFormat();
}
