package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Builder
@Getter
public class ImageTransformationOperations implements TransformationOperations {
    @Nullable
    private final LimitResolution limitWidth;
    @Nullable
    private final LimitResolution limitHeight;
    /** Output format, defaults to JPEG. */
    @NonNull
    @Builder.Default
    private final FileType format = FileType.JPEG;
    /** Output quality, defaults to 100. Applicable only when the format supports it. */
    @NonNull
    @Builder.Default
    private final Integer quality = 100;
    @Nullable
    private final AspectRatio aspectRatio;
}
