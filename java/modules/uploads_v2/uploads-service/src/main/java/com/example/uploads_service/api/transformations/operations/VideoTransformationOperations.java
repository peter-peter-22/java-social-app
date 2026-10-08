package com.example.uploads_service.api.transformations.operations;

import com.example.uploads_service.api.uploads.FileType;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record VideoTransformationOperations(
        VideoEncodings.@NonNull VideoEncoding encoding
) implements TransformationOperations {

    @Override
    public @NonNull FileType getFormat() {
        return encoding.getFileType();
    }
}
