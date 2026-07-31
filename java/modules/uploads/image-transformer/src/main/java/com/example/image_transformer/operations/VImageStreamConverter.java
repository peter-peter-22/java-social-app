package com.example.image_transformer.operations;

import app.photofox.vipsffm.VBlob;
import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.uploads_api.transformations.operations.ImageEncodings;
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

    @NonNull InputStream toStream(@NonNull VImage image, ImageEncodings.@NonNull ImageEncoding outputFormat) {
        var outputData = saveToBuffer(image, outputFormat);
        return new ByteArrayInputStream(outputData.getBytes());
    }

    private @NonNull VBlob saveToBuffer(
            @NonNull VImage image,
            ImageEncodings.@NonNull ImageEncoding format
    ) {
        return switch (format) {
            case ImageEncodings.Jpeg encoding -> image.jpegsaveBuffer(
                    VipsOption.Int("Q", encoding.quality())
            );
            case ImageEncodings.Webp encoding -> image.webpsaveBuffer(
                    VipsOption.Int("Q", encoding.quality())
            );
        };
    }
}
