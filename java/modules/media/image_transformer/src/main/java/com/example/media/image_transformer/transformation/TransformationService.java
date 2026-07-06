package com.example.media.image_transformer.transformation;

import app.photofox.vipsffm.Vips;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransformationService {
    private final TransformationImageStorage imageStorage;
    private final ImageTransformationPipeline transformationPipeline;

    public void applyTransformations(@NotNull UploadTransformationDTO upload) {
        var operations = (ImageTransformationOperations) upload.operations();

        Vips.run(arena -> {
            var input = imageStorage.input(upload);
            var image = transformationPipeline.apply(arena, input, operations);
            imageStorage.write(image, upload, operations);
        });
    }
}
