package com.example.image_transformer.task_service;

import com.example.image_transformer.transformation.ImageTransformationService;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.operations.AspectRatio;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ImageTransformationServiceTests {
    private final ImageTransformationService imageTransformationService = new ImageTransformationService();
    private final Path inputFile = Path.of("./src/test/resources/test-images/image.jpg");
    private final Path outputDir = Path.of("./src/test/resources/test-images/output");

    @Test
    void inputFileExists() throws IOException {
        System.out.println("Work directory: " + Path.of("").toAbsolutePath());
        assertThat(Files.exists(inputFile)).isTrue();
    }

    @Test
    void aspectFillProducesSquare() throws IOException {
        imageTransformationService.transformFile(
                inputFile.toString(),
                outputDir.resolve("aspectFillProducesSquare"),
                ImageTransformationOperations.builder()
                        .aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.FILL))
                        .build()
        );
    }

    void aspectContainPadsToSquare() throws IOException {

    }

    void limitWidthProducesExpectedResolution() throws IOException {

    }

    void quality95Executes() {

    }

    void quality50Executes() {

    }

    void quality50WebpExecutes() {

    }
}
