package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import org.jetbrains.annotations.NotNull;

public interface TransformationOperations {
    @NotNull FileType getFormat();
}
