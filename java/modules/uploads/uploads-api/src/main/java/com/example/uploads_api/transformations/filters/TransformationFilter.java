package com.example.uploads_api.transformations.filters;

import com.example.uploads_api.uploads.Upload;
import org.jetbrains.annotations.NotNull;

public interface TransformationFilter {
    boolean isApplicable(@NotNull Upload upload);
}
