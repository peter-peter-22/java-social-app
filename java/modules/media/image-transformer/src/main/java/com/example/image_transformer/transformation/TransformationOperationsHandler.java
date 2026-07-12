package com.example.image_transformer.transformation;

import app.photofox.vipsffm.Vips;
import com.example.image_transformer.storage.TransformationImageStorage;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.task.UploadTransformationTask;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TransformationOperationsHandler {
    private final TransformationImageStorage imageStorage;
    private final ImageTransformationPipeline transformationPipeline;
    private final VImageStreamConverter vImageStreamConverter;

    public void applyTransformationOperations(@NotNull UploadTransformationTask upload) {
        var operations = (ImageTransformationOperations) upload.getOperations();
        Vips.run(arena -> {
            try (var inputStream = imageStorage.read(upload)) {
                var input = vImageStreamConverter.fromStream(arena, inputStream);
                var image = transformationPipeline.apply(input, operations);

                try (var outputStream = vImageStreamConverter.toStream(image, operations)) {
                    imageStorage.write(outputStream, upload);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to transform image", e);
            }
        });
    }
}
