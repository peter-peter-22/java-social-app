package com.example.image_transformer.operations;

import app.photofox.vipsffm.VImage;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ImageTransformationPipeline {
    private final ImageTransformation[] transformations;

    @NonNull
    VImage apply(
            @NonNull VImage input,
            @NonNull ImageTransformationOperations operations
    ) {
        for (var transformation : transformations) {
            input = transformation.apply(input, operations);
        }
        return input;
    }
}
