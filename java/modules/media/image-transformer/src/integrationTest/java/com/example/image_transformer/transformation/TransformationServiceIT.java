package com.example.image_transformer.transformation;

import com.example.image_transformer.TestApplication;
import com.example.media_api.transformations.operations.AspectRatio;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.LimitResolution;
import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.UploadId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles({"local"})
@TestPropertySource(locations = "classpath:image-transformation-test.properties")
class TransformationServiceIT {
    private static final UploadId INPUT = new UploadId("image.jpg", "uploads");
    private static final String BUCKET = "transformations";

    @Autowired
    private TransformationOperationsHandler transformationOperationsHandler;

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
    void quality50Executes() {
        transform("quality-50", ops -> ops.quality(50));
    }

    @Test
    void quality50WebpExecutes() {
        transform("quality-50-webp", ops -> {ops.quality(50);ops.format(FileType.WEBP);});
    }

    private void assertSize(
            String name,
            int expectedWidth,
            int expectedHeight,
            Consumer<ImageTransformationOperations.ImageTransformationOperationsBuilder> customize
    ) throws IOException {
        var transformation = transform(name, customize);
        var path = resourcesDirectory().resolve(transformation.getOutputObject().path()).toFile();
        var image = ImageIO.read(path);
        assertNotNull(image);
        assertEquals(expectedWidth, image.getWidth());
        assertEquals(expectedHeight, image.getHeight());
    }

    private UploadTransformationTask transform(
            String name,
            Consumer<ImageTransformationOperations.ImageTransformationOperationsBuilder> customize
    ) {
        var transformation = upload(name, customize);
        transformationOperationsHandler.applyTransformationOperations(transformation);
        return transformation;
    }

    private UploadTransformationTask upload(
            String name,
            @NonNull Consumer<ImageTransformationOperations.ImageTransformationOperationsBuilder> customize
    ) {
        var builder = ImageTransformationOperations.builder();
        customize.accept(builder);
        return UploadTransformationTask.builder()
                .name(name)
                .outputBucket(BUCKET)
                .original(INPUT)
                .operations(builder.build())
                .lazy(false)
                .build();
    }

    // TODO: the same as LocalImageTransformationStorage
    private @NonNull Path resourcesDirectory() {
        var integrationRelative = Path.of("src/integrationTest/resources");
        if (java.nio.file.Files.isDirectory(integrationRelative)) {
            return integrationRelative;
        }

        var moduleIntegrationRelative = Path.of("modules/media/image-transformer/src/integrationTest/resources");
        if (java.nio.file.Files.isDirectory(moduleIntegrationRelative)) {
            return moduleIntegrationRelative;
        }

        throw new IllegalStateException("Cannot locate image-transformer integration test resources");
    }
}
