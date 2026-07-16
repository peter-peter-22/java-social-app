package com.example.media_api.transformations.filters;

import com.example.media_api.uploads.Upload;
import org.jetbrains.annotations.NotNull;

public interface TransformationFilter {
    boolean isApplicable(@NotNull Upload upload);
}
