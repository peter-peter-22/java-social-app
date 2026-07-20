package com.example.uploads_api.transformations.filters;

import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NonNull;

public interface TransformationFilter {
    boolean isApplicable(@NonNull Upload upload);
}
