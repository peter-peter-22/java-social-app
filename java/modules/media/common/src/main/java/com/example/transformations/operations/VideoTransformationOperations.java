package com.example.transformations.operations;

import com.example.uploads.FileType;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Builder
@Getter
public class VideoTransformationOperations extends UploadTransformationOperations {
    @Nullable
    private final LimitResolution limitWidth;
    @Nullable
    private final LimitResolution limitHeight;
    @Nullable
    private final FileType format;
    @Builder.Default
    private final Integer quality = 100;
    @Nullable
    private final AspectRatio aspectRatio;
}
