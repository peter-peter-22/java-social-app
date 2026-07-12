package com.example.media_api.transformations.operations;

import com.example.media_api.uploads.FileType;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Builder
@Getter
public class VideoTransformationOperations extends UploadTransformationOperations {
    @Nullable
    private final LimitResolution limitWidth;
    @Nullable
    private final LimitResolution limitHeight;
    @NotNull @Builder.Default
    private final FileType format = FileType.MP4;
    @Builder.Default
    private final Integer quality = 100;
    @Nullable
    private final AspectRatio aspectRatio;
}
