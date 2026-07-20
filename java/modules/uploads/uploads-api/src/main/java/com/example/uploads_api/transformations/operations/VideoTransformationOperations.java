package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Builder
@Getter
public class VideoTransformationOperations implements TransformationOperations {
    @Nullable
    private final LimitResolution limitWidth;
    @Nullable
    private final LimitResolution limitHeight;
    @NonNull
    @Builder.Default
    private final FileType format = FileType.MP4;
    @NonNull
    @Builder.Default
    private final Integer quality = 100;
    @Nullable
    private final AspectRatio aspectRatio;
}
