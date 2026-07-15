package com.example.media_api.transformations.dto;

import com.example.media_api.transformations.operations.AspectRatio;
import com.example.media_api.transformations.operations.LimitResolution;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.ObjectLocation;
import com.example.media_api.uploads.UploadId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record ImageTransformationTaskDTO(
        @NotNull ObjectLocation inputObject,
        @NotNull ObjectLocation outputObject,
        @NotNull String name,
        boolean lazy,
        @Nullable LimitResolution limitWidth,
        @Nullable LimitResolution limitHeight,
        @NotNull FileType format,
        int quality,
        @Nullable AspectRatio aspectRatio,
        @NonNull UploadId uploadId
) {
}
