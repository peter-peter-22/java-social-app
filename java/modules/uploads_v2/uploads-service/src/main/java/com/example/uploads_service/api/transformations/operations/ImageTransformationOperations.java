package com.example.uploads_service.api.transformations.operations;

import com.example.image_transformer.api.transformations.operations.AspectRatio;
import com.example.image_transformer.api.transformations.operations.ImageEncodings;
import com.example.image_transformer.api.transformations.operations.TransformationOperations;
import com.example.image_transformer.api.uploads.FileType;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Builder
@NullMarked
public record ImageTransformationOperations(
        @Nullable
        LimitResolution limitWidth,
        @Nullable
        LimitResolution limitHeight,
        @Nullable
        AspectRatio aspectRatio,
        ImageEncodings.ImageEncoding encoding
) implements TransformationOperations {

    @Override
    public FileType getFormat() {
        return encoding.getFileType();
    }
}
