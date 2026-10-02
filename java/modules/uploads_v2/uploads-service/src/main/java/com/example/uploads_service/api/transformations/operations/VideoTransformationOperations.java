package com.example.uploads_service.api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record VideoTransformationOperations(
        VideoEncodings.@NonNull VideoEncoding encoding
) implements TransformationOperations {

    @Override
    public @NonNull FileType getFormat() {
        return encoding.fileType();
    }

    public static VideoTransformationOperationsBuilder builderWithDefaults() {
        return builder()
                .encoding(VideoEncodings.Mp4.builder().build());
    }
}
