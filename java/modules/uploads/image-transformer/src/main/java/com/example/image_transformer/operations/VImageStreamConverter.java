package com.example.image_transformer.operations;

import app.photofox.vipsffm.VBlob;
import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.uploads_api.uploads.FileType;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.foreign.Arena;

@Component
class VImageStreamConverter {
    @NonNull VImage fromStream(@NonNull Arena arena, @NonNull InputStream inputStream) {
        return VImage.newFromStream(
                arena,
                inputStream,
                VipsOption.Boolean("autorotate", true)
        );
    }

    @NonNull InputStream toStream(@NonNull VImage image, @NonNull FileType outputFormat, int quality) {
        var outputData = saveToBuffer(image, outputFormat, quality);
        return new ByteArrayInputStream(outputData.getBytes());
    }

    private @NonNull VBlob saveToBuffer(
            @NonNull VImage image,
            @NonNull FileType format,
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
