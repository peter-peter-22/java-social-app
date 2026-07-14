package com.example.media_api.transformations.operations;

import com.example.media_api.uploads.FileType;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Builder
@Getter
public class ImageTransformationOperations implements TransformationOperations {
    @Nullable
    private final LimitResolution limitWidth;
    @Nullable
    private final LimitResolution limitHeight;
    /** Output format, defaults to JPEG. */
    @NotNull
    @Builder.Default
    private final FileType format = FileType.JPEG;
    /** Output quality, defaults to 100. Applicable only when the format supports it. */
    @NotNull @Builder.Default
    private final Integer quality = 100;
    @Nullable
    private final AspectRatio aspectRatio;
}
