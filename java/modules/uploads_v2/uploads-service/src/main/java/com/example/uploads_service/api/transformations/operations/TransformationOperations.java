package com.example.uploads_service.api.transformations.operations;

import com.example.uploads_service.api.uploads.FileType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface TransformationOperations {
    FileType getFormat();
}
