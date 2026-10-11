package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;

@Builder
@NullMarked
public record VideoTransformationOperations(
        VideoEncodings.VideoEncoding encoding
) implements PolymorphicTransformationOperations {

    @Override
    public FileType getFormat() {
        return encoding.getFileType();
    }
}
