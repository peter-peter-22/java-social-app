package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Builder
@Getter
public class VideoTransformationOperations implements TransformationOperations {
    @Nullable
    private final LimitResolution limitWidth;
    @Nullable
    private final LimitResolution limitHeight;
    @NotNull @Builder.Default
    private final FileType format = FileType.MP4;
    @NotNull @Builder.Default
    private final Integer quality = 100;
    @Nullable
    private final AspectRatio aspectRatio;
}
