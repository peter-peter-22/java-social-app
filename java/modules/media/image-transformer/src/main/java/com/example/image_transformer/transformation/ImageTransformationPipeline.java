package com.example.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.foreign.Arena;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
class ImageTransformationPipeline {
    private final ImageTransformation[] transformations;

    @NotNull
    VImage apply(
            @NotNull Arena arena,
            @NotNull Path input,
            @NotNull ImageTransformationOperations operations
    ) {
        var image = VImage.newFromFile(
                arena,
                input.toString(),
                VipsOption.Boolean("autorotate", true)
        );

        for (var transformation : transformations) {
            image = transformation.apply(image, operations);
        }
        return image;
    }
}
