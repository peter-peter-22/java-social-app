package com.example.image_transformer.operations;

import app.photofox.vipsffm.VImage;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import org.jspecify.annotations.NonNull;

interface ImageTransformation {
    @NonNull
    VImage apply(@NonNull VImage image, @NonNull ImageTransformationOperations operations);
}
