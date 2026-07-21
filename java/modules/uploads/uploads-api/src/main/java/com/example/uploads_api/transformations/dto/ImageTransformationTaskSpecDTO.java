package com.example.uploads_api.transformations.dto;

import com.example.uploads_api.transformations.operations.AspectRatio;
import com.example.uploads_api.transformations.operations.LimitResolution;
import com.example.uploads_api.uploads.FileType;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ImageTransformationTaskSpecDTO(
        @NonNull ObjectLocation outputObject,
        @NonNull String name,
        boolean lazy,
        @Nullable LimitResolution limitWidth,
        @Nullable LimitResolution limitHeight,
        @NonNull FileType format,
        int quality,
        @Nullable AspectRatio aspectRatio,
        @NonNull UploadId uploadId
) {
}
