package com.example.image_transformer.operations;

import app.photofox.vipsffm.VImage;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import org.jetbrains.annotations.NotNull;

interface ImageTransformation {
    @NotNull
    VImage apply(@NotNull VImage image, @NotNull ImageTransformationOperations operations);
}
