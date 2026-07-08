package com.example.image_transformer.storage;

import app.photofox.vipsffm.VImage;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.Arena;

public interface TransformationImageStorage {
    void write(
            @NotNull VImage image,
            @NotNull UploadTransformationDTO upload,
            @NotNull ImageTransformationOperations operations
    );

    @NotNull VImage read(@NotNull Arena arena, @NotNull UploadTransformationDTO upload);
}
