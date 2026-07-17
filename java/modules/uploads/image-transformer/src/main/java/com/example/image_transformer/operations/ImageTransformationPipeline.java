package com.example.image_transformer.operations;

import app.photofox.vipsffm.VImage;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ImageTransformationPipeline {
    private final ImageTransformation[] transformations;

    @NotNull
    VImage apply(
            @NotNull VImage input,
            @NotNull ImageTransformationOperations operations
    ) {
        for (var transformation : transformations) {
            input = transformation.apply(input, operations);
        }
        return input;
    }
}
