package com.example.image_transformer.operations;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.Vips;
import app.photofox.vipsffm.VipsOption;
import com.example.uploads_api.v2.transformations.operations.ImageEncodings;
import com.example.uploads_api.v2.transformations.operations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ImageTransformationService {
    private final ImageTransformationPipeline pipeline;

    public void transformFile(String inputFile, Path outputDir, @NonNull ImageTransformationOperations operations) {
        Vips.run(arena -> {
            var inputVImage = VImage.newFromFile(arena, inputFile);

            var outputVImage = pipeline.apply(inputVImage, operations);

            var path = outputDir.resolve("default." + operations.encoding().fileType().getExtension());
            var pathString = path.toString();
            switch (operations.encoding()) {
                case ImageEncodings.Jpeg encoding -> outputVImage.jpegsave(
                        pathString,
                        VipsOption.Int("Q", encoding.quality())
                );
                case ImageEncodings.Webp encoding -> outputVImage.webpsave(
                        pathString,
                        VipsOption.Int("Q", encoding.quality())
                );
            }
        });
    }
}
