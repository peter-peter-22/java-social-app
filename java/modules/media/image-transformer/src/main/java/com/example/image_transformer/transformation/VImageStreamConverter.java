package com.example.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.foreign.Arena;

@Component
class VImageStreamConverter {
    @NotNull VImage fromStream(@NotNull Arena arena, @NotNull InputStream inputStream) {
        return VImage.newFromStream(
                arena,
                inputStream,
                VipsOption.Boolean("autorotate", true)
        );
    }

    @NotNull InputStream toJpegStream(@NotNull VImage image, @NotNull ImageTransformationOperations operations) {
        var jpegData = image.jpegsaveBuffer(
                VipsOption.Int("Q", operations.getQuality() == null ? 100 : operations.getQuality())
        );
        return new ByteArrayInputStream(jpegData.getBytes());
    }
}
