package com.example.media_api.transformations.operations;

import com.example.media_api.uploads.FileType;
import org.jetbrains.annotations.NotNull;

public interface TransformationOperations {
    @NotNull FileType getFormat();
}
