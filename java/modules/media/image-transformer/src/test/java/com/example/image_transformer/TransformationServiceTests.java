package com.example.image_transformer;

import com.example.image_transformer.transformation.TransformationService;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.AspectRatio;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.LimitResolution;
import com.example.media_api.uploads.UploadId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TransformationServiceTests {
    private static final UploadId INPUT = new UploadId("image.jpg", "uploads");
    private static final String BUCKET = "transformations";

    @Autowired
    private TransformationService transformationService;

    @Test
    void aspectFillProducesSquare() throws IOException {
        assertSize("aspect-fill-square", 852, 852,
                ops -> ops.aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.FILL)));
    }

    @Test
    void aspectContainPadsToSquare() throws IOException {
        assertSize("aspect-contain-square", 1280, 1280,
                ops -> ops.aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.CONTAIN)));
    }

    @Test
    void limitWidthProducesExpectedResolution() throws IOException {
        assertSize("limit-width-400", 400, 266,
                ops -> ops.limitWidth(new LimitResolution(400, LimitResolution.Mode.KEEP_ASPECT_RATIO)));
    }

    @Test
    void quality95Executes() {
        transform("quality-95", ops -> ops.quality(95));
    }

    @Test
    void quality70Executes() {
        transform("quality-50", ops -> ops.quality(50));
    }

    private void assertSize(
            String name,
            int expectedWidth,
            int expectedHeight,
            Consumer<ImageTransformationOperations.ImageTransformationOperationsBuilder> customize
    ) throws IOException {
        transform(name, customize);
        var image = ImageIO.read(output(name).toFile());
        assertNotNull(image);
        assertEquals(expectedWidth, image.getWidth());
        assertEquals(expectedHeight, image.getHeight());
    }

    private void transform(
            String name,
            Consumer<ImageTransformationOperations.ImageTransformationOperationsBuilder> customize
    ) {
        transformationService.applyTransformations(upload(name, customize));
    }

    private UploadTransformationDTO upload(
            String name,
            Consumer<ImageTransformationOperations.ImageTransformationOperationsBuilder> customize
    ) {
        var builder = ImageTransformationOperations.builder();
        customize.accept(builder);
        return new UploadTransformationDTO(name, BUCKET, INPUT, null, builder.build());
    }

    private Path output(String name) {
        return Path.of("src/test/resources/output_" + BUCKET + "_" + name + ".jpg");
    }
}
