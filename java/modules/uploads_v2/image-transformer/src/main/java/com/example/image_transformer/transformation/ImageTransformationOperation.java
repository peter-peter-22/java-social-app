package com.example.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import org.jspecify.annotations.NullMarked;

@NullMarked
interface ImageTransformationOperation {
    VImage apply(VImage image, ImageTransformationOperations operations);
}
