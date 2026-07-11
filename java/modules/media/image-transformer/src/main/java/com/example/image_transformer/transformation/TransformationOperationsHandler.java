package com.example.image_transformer.transformation;

import app.photofox.vipsffm.Vips;
import com.example.image_transformer.storage.TransformationImageStorage;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TransformationOperationsHandler {
    private final TransformationImageStorage imageStorage; // TODO select with profile
    private final ImageTransformationPipeline transformationPipeline;

    public void applyTransformationOperations(@NotNull UploadTransformationDTO upload) {
        var operations = (ImageTransformationOperations) upload.operations();
        Vips.run(arena -> {
            var input = imageStorage.read(arena, upload);
            var image = transformationPipeline.apply(input, operations);
            imageStorage.write(image, upload, operations);
        });
    }
}
