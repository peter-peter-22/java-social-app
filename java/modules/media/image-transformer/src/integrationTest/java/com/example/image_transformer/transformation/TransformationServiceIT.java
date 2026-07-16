package com.example.image_transformer.transformation;

import com.example.image_transformer.TestApplication;
import com.example.image_transformer.storage.LocalStreamStorage;
import com.example.image_transformer.task.ImageTransformationTask;
import com.example.image_transformer.task.TestTaskCreator;
import com.example.media_api.transformations.operations.AspectRatio;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.LimitResolution;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.ObjectLocation;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles({"local"})
@TestPropertySource(locations = "classpath:image-transformation-test.properties")
class TransformationServiceIT {
    private static final ObjectLocation TEST_FILE = new ObjectLocation("image.jpg", "test-images");
    private static final int ORIGINAL_WIDTH = 1280;
    private static final int ORIGINAL_HEIGHT = 852;
    @Autowired
    private TaskService service;

    @Test
    void aspectFillProducesSquare() throws IOException {
        var task = localTestTask("aspect-fill-square", c -> c.aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.FILL)));
        executeTask(task);
        assertSize(ORIGINAL_HEIGHT, ORIGINAL_HEIGHT, task);
    }

    @Test
    void aspectContainPadsToSquare() throws IOException {
        var task = localTestTask("aspect-contain-square", c -> c.aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.CONTAIN)));
        executeTask(task);
        assertSize(ORIGINAL_WIDTH, ORIGINAL_WIDTH, task);
    }

    @Test
    void limitWidthProducesExpectedResolution() throws IOException {
        var task = localTestTask("limit-width-400", c -> c.limitWidth(new LimitResolution(400, LimitResolution.Mode.KEEP_ASPECT_RATIO)));
        executeTask(task);
        assertSize(400, 266, task);
    }

    @Test
    void quality95Executes() {
        executeTask(localTestTask("quality-95", c -> c.quality(95)));
    }

    @Test
    void quality50Executes() {
        executeTask(localTestTask("quality-50", c -> c.quality(50)));
    }

    @Test
    void quality50WebpExecutes() {
        executeTask(localTestTask("format-webp", c -> c.format(FileType.WEBP)));
    }

    private void assertSize(
            int expectedWidth,
            int expectedHeight,
            @NonNull ImageTransformationTask task
    ) throws IOException {
        var outputFile = LocalStreamStorage.objectLocationToLocalPath(task.outputObject()).toFile();
        var image = ImageIO.read(outputFile);
        System.out.println("file: " + outputFile.getAbsolutePath());
        System.out.println("read: "+image.getWidth() + " " + image.getHeight() + "\nexpected: " + expectedWidth + " " + expectedHeight + "\n");
        assertNotNull(image);
        assertEquals(expectedWidth, image.getWidth());
        assertEquals(expectedHeight, image.getHeight());
    }

    private void executeTask(
            @NonNull ImageTransformationTask task
    ) {
        service.processTasks(List.of(task));
    }

    private @NonNull ImageTransformationTask localTestTask(
            @NonNull String name,
            Consumer<ImageTransformationOperations.@NonNull ImageTransformationOperationsBuilder> customizer
    ) {
        var operationsBuilder = ImageTransformationOperations.builder();

        if (customizer != null)
            customizer.accept(operationsBuilder);

        var operations = operationsBuilder.build();
        var extension = operations.getFormat().getExtensions()[0];

        return TestTaskCreator.createTask(
                c -> {
                    c.inputObject(TEST_FILE);
                    c.outputObject(new ObjectLocation(name + "." + extension, TEST_FILE.bucket()));
                    c.operations(operations);
                    c.name(name);
                }
        );
    }
}
