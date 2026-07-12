package com.example.image_transformer.transformation;

import app.photofox.vipsffm.Vips;
import com.example.image_transformer.storage.TransformationImageStorage;
import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TransformationOperationsHandler {
    private final TransformationImageStorage imageStorage; // TODO select with profile
    private final ImageTransformationPipeline transformationPipeline;

    public void applyTransformationOperations(@NotNull UploadTransformationTask upload) {
        var operations = (ImageTransformationOperations) upload.getOperations();
        Vips.run(arena -> {
            var input = imageStorage.read(arena, upload);
            var image = transformationPipeline.apply(input, operations);
            imageStorage.write(image, upload, operations);
        });
    }
}
