package com.example.image_transformer.task_service;

import com.example.image_transformer.transformation.ImageTransformationService;
import com.example.uploads_api.transformations.ImageEncodings;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.operations.AspectRatio;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class ImageTransformationServiceTests {
    private final ImageTransformationService imageTransformationService = new ImageTransformationService();
    private final Path inputFile = Path.of("./src/test/resources/test-images/image.jpg");
    private final Path outputDir = Path.of("./src/test/resources/test-images/output");

    @Test
    void inputFileExists() {
        System.out.println("Work directory: " + Path.of("").toAbsolutePath());
        assertThat(Files.exists(inputFile)).isTrue();
    }

    @Test
    void aspectFillProducesSquare() throws IOException {
        var result = runTransformationTest(
                "aspectFillProducesSquare",
                builder -> builder.aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.FILL))
        );
        float widthToHeightRatio = result.metadata().getResolutionX() * 1f / result.metadata().getResolutionY();
        Assertions.assertThat(areEqual(widthToHeightRatio, 1f, 0.01f)).isTrue();
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

    ImageTransformationService.TransformationResult runTransformationTest(
            String name,
            Consumer<ImageTransformationOperations.ImageTransformationOperationsBuilder> customizer
    ) throws IOException {

        var testDir = outputDir.resolve(name);
        Files.createDirectories(testDir);

        var builder = ImageTransformationOperations.builder()
                .encoding(ImageEncodings.Jpeg.builder().quality(100).build());
        if (customizer != null)
            customizer.accept(builder);

        return imageTransformationService.transformFile(
                inputFile.toString(),
                testDir,
                builder.build()
        );
    }

    private static boolean areEqual(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }
}
