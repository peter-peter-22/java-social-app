package com.example.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.media_api.uploads.FileType;
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

    @NotNull InputStream toStream(@NotNull VImage image, @NotNull ImageTransformationOperations operations) {
        var outputData = saveToBuffer(image, operations.getFormat(), operations.getQuality());
        return new ByteArrayInputStream(outputData.getBytes());
    }

    @NotNull
    private app.photofox.vipsffm.VBlob saveToBuffer(
            @NotNull VImage image,
            @NotNull FileType format,
            Integer quality
    ) {
        var outputQuality = VipsOption.Int("Q", quality == null ? 100 : quality);

        return switch (format) {
            case JPEG -> image.jpegsaveBuffer(outputQuality);
            case WEBP -> image.webpsaveBuffer(outputQuality);
            default -> throw new IllegalArgumentException("Unsupported image output format: " + format);
        };
    }
}
