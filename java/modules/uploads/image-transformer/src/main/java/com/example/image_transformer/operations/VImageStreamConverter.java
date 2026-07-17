package com.example.image_transformer.operations;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.uploads_api.uploads.FileType;
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

    @NotNull InputStream toStream(@NotNull VImage image, @NotNull FileType outputFormat, int quality) {
        var outputData = saveToBuffer(image, outputFormat, quality);
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
