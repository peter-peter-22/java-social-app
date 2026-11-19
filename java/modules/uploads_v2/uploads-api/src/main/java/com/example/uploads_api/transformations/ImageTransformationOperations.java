package com.example.uploads_api.transformations;

import com.example.uploads_api.transformations.operations.AspectRatio;
import com.example.uploads_api.transformations.operations.LimitResolution;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Builder
@NullMarked
@Jacksonized
public record ImageTransformationOperations(
        @Nullable
        LimitResolution limitWidth,
        @Nullable
        LimitResolution limitHeight,
        @Nullable
        AspectRatio aspectRatio,
        ImageEncodings.ImageEncoding encoding
) implements PolymorphicTransformationOperations {
}
