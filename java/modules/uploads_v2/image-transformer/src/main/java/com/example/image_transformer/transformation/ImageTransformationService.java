package com.example.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.Vips;
import app.photofox.vipsffm.VipsOption;
import com.example.uploads_api.transformations.ImageEncodings;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@NullMarked
public class ImageTransformationService {
    private final ImageTransformationPipeline pipeline;

    /**
     * Write the resulting files of a transformation into the work directory, return the entrypoint.
     */
    public Path transformFile(String inputFilePath, Path workDir, ImageTransformationOperations operations) {
        AtomicReference<@Nullable Path> entryPoint = new AtomicReference<>();

        Vips.run(arena -> {
            var inputVImage = VImage.newFromFile(arena, inputFilePath);
            var outputVImage = pipeline.apply(inputVImage, operations);

            switch (operations.encoding()) {
                case ImageEncodings.Jpeg encoding -> {
                    var path = workDir.resolve(Path.of("default.jpg"));
                    outputVImage.jpegsave(
                            path.toString(),
                            VipsOption.Int("Q", encoding.getQuality())
                    );
                    entryPoint.set(path);
                }
                case ImageEncodings.Webp encoding -> {
                    var path = workDir.resolve(Path.of("default.webp"));
                    outputVImage.webpsave(
                            path.toString(),
                            VipsOption.Int("Q", encoding.getQuality())
                    );
                    entryPoint.set(path);
                }
            }
        });

        return Objects.requireNonNull(entryPoint.get());
    }
}
