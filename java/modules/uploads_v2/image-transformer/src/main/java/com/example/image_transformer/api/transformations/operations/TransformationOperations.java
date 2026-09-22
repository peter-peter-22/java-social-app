package com.example.image_transformer.api.transformations.operations;

import com.example.image_transformer.api.uploads.FileType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface TransformationOperations {
    FileType getFormat();
}
