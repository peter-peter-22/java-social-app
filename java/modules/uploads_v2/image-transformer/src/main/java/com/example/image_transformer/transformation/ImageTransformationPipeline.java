package com.example.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
class ImageTransformationPipeline {
    private final ImageTransformationOperation[] transformations =  new ImageTransformationOperation[]{
            new AspectRatioTransformation(),
            new LimitResolutionTransformation()
    };

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
