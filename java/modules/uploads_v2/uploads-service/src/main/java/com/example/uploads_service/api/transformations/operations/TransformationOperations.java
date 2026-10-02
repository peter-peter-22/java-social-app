package com.example.uploads_service.api.transformations.operations;

import com.example.image_transformer.api.uploads.FileType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface TransformationOperations {
    FileType getFormat();
}
