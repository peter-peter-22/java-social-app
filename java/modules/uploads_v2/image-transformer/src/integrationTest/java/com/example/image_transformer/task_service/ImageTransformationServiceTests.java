package com.example.image_transformer.task_service;

import com.example.image_transformer.transformation.ImageTransformationService;
import com.example.uploads_api.transformations.ImageEncodings;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.operations.AspectRatio;
import com.example.uploads_api.transformations.operations.LimitResolution;
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

    @Test
    void aspectContainPadsToSquare() throws IOException {
        var result = runTransformationTest(
                "aspectContainPadsToSquare",
                builder -> builder.aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.CONTAIN))
        );
        float widthToHeightRatio = result.metadata().getResolutionX() * 1f / result.metadata().getResolutionY();
        Assertions.assertThat(areEqual(widthToHeightRatio, 1f, 0.01f)).isTrue();
        assertThat(result.entryPoint()).exists().isRegularFile();
    }

    @Test
    void limitWidthProducesExpectedResolution() throws IOException {
        int limitWidth = 640;
        var result = runTransformationTest(
                "limitWidthProducesExpectedResolution",
                builder -> builder.limitWidth(new LimitResolution(limitWidth, LimitResolution.Mode.KEEP_ASPECT_RATIO))
        );
        assertThat(result.metadata().getResolutionX()).isEqualTo(limitWidth);
        // Source image is 1280x852, so a width limit of 640 keeps the aspect ratio at half height.
        assertThat(result.metadata().getResolutionY()).isEqualTo(426);
    }

    @Test
    void quality50Executes() throws IOException {
        var result = runTransformationTest(
                "quality50Executes",
                builder -> builder.encoding(ImageEncodings.Jpeg.builder().quality(50).build())
        );
        assertThat(result.entryPoint())
                .exists()
                .isRegularFile()
                .hasFileName("default.jpg");
        assertThat(result.metadata().getResolutionX()).isPositive();
        assertThat(result.metadata().getResolutionY()).isPositive();
    }

    @Test
    void quality50WebpExecutes() throws IOException {
        var result = runTransformationTest(
                "quality50WebpExecutes",
                builder -> builder.encoding(ImageEncodings.Webp.builder().quality(50).build())
        );
        assertThat(result.entryPoint())
                .exists()
                .isRegularFile()
                .hasFileName("default.webp");
        assertThat(result.metadata().getResolutionX()).isPositive();
        assertThat(result.metadata().getResolutionY()).isPositive();
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
