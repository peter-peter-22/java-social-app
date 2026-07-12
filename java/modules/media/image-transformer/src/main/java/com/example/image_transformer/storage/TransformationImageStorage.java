package com.example.image_transformer.storage;

import app.photofox.vipsffm.VImage;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.task.UploadTransformationTask;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.Arena;

public interface TransformationImageStorage {
    void write(
            @NotNull VImage image,
            @NotNull UploadTransformationTask upload,
            @NotNull ImageTransformationOperations operations
    );

    @NotNull VImage read(@NotNull Arena arena, @NotNull UploadTransformationTask upload);
}
