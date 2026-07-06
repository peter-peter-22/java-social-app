package com.example.media.image_transformer;

import app.photofox.vipsffm.VImage;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

interface TransformationImageStorage {
    @NotNull Path input(@NotNull UploadTransformationDTO upload);

    void write(
            @NotNull VImage image,
            @NotNull UploadTransformationDTO upload,
            @NotNull ImageTransformationOperations operations
    );
}
