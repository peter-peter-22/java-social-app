package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface TransformationOperations {
    @JsonIgnore
    FileType getFormat();
}
