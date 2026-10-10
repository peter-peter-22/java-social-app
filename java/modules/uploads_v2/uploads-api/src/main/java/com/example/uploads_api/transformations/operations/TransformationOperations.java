package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface TransformationOperations {
    FileType getFormat();
}
