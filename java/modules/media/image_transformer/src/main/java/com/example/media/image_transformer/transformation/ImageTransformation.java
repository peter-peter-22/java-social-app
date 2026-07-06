package com.example.media.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import org.jetbrains.annotations.NotNull;

interface ImageTransformation {
    @NotNull
    VImage apply(@NotNull VImage image, @NotNull ImageTransformationOperations operations);
}
