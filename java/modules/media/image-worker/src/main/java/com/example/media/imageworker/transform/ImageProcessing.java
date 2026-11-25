package com.example.media.imageworker.transform;

import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.transformation.ImageTransformationSpec;

import java.nio.file.Path;

/**
 * The image processing port of the worker: read the dimensions of a file and apply one resolved
 * transformation to it. The production implementation is the libvips adapter
 * ({@link ImageProcessor}); the use case tests substitute an in-memory fake.
 */
public interface ImageProcessing {

    /** The encoded result of one transformation: the entry file and the dimensions of the image. */
    record ProcessedFile(Path entryPoint, ImageMetadata metadata) {
    }

    /** Reads the dimensions of an image file. */
    ImageMetadata readMetadata(Path file);

    /**
     * Applies the resolved operations to the source image and writes the encoded result into the
     * output directory. The aspect ratio is applied before the resolution limit.
     */
    ProcessedFile process(Path source, Path outputDir, ImageTransformationSpec spec);
}