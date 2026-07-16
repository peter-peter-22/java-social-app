package com.example.media_api.transformations.dto;

import com.example.media_api.transformations.operations.AspectRatio;
import com.example.media_api.transformations.operations.LimitResolution;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.ObjectLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VideoTransformationTaskDTO(
        @NotNull ObjectLocation inputObject,
        @NotNull ObjectLocation outputObject,
        @NotNull String name,
        boolean lazy,
        @NotNull String outputBucket,
        @Nullable LimitResolution limitWidth,
        @Nullable LimitResolution limitHeight,
        @NotNull FileType format,
        int quality,
        @Nullable AspectRatio aspectRatio
) {
}
