package com.example.transformations;

import com.example.uploads.Upload;
import org.jetbrains.annotations.NotNull;

public interface TransformationFilter {
    boolean isApplicable(@NotNull Upload upload);
}
