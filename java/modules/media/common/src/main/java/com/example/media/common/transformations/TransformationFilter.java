package com.example.media.common.transformations;

import com.example.media.common.uploads.Upload;
import org.jetbrains.annotations.NotNull;

public interface TransformationFilter {
    boolean isApplicable(@NotNull Upload upload);
}
