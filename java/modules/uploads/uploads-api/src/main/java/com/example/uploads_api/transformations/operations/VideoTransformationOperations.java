package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Builder
public record VideoTransformationOperations(
        @Nullable
        LimitResolution limitWidth,
        @Nullable
        LimitResolution limitHeight,
        VideoEncodings.@NonNull VideoEncoding encoding
) implements TransformationOperations {

    @Override
    public @NonNull FileType getFormat() {
        return encoding.fileType();
    }

    public static VideoTransformationOperationsBuilder builderWithDefaults() {
        return builder()
                .limitWidth(null)
                .limitHeight(null)
                .encoding(VideoEncodings.Mp4.builder().build());
    }
}
