package com.example.uploads_api.transformations.dto;

import com.example.uploads_api.transformations.operations.AspectRatio;
import com.example.uploads_api.transformations.operations.LimitResolution;
import com.example.uploads_api.uploads.FileType;
import com.example.uploads_api.uploads.ObjectLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record VideoTransformationTaskDTO(
        @NonNull ObjectLocation inputObject,
        @NonNull ObjectLocation outputObject,
        @NonNull String name,
        boolean lazy,
        @NonNull String outputBucket,
        @Nullable LimitResolution limitWidth,
        @Nullable LimitResolution limitHeight,
        @NonNull FileType format,
        int quality,
        @Nullable AspectRatio aspectRatio
) {
}
