package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Builder
public record ImageTransformationOperations(
        @Nullable
        LimitResolution limitWidth,
        @Nullable
        LimitResolution limitHeight,
        @Nullable
        AspectRatio aspectRatio,
        ImageEncodings.@NonNull ImageEncoding encoding
) implements TransformationOperations {

    @Override
    public @NonNull FileType getFormat() {
        return encoding.fileType();
    }

    public static @NonNull ImageTransformationOperationsBuilder builderWithDefaults() {
        return builder()
                .encoding(
                        ImageEncodings.Jpeg.builderWithDefaults().build()
                );
    }
}
