package com.example.image_transformer.storage;

import com.example.media_api.transformations.task.UploadTransformationTask;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public interface TransformationImageStorage {
    void write(
            @NotNull InputStream inputStream,
            @NotNull UploadTransformationTask upload
    );

    @NotNull InputStream read(@NotNull UploadTransformationTask upload);
}
