package com.example.image_transformer.task_service;

import com.example.image_transformer.TestApplication;
import com.example.transformer_contracts.storage.LocalStorageConfiguration;
import com.example.transformer_contracts.storage.LocalStreamStorage;
import com.example.uploads_api.transformations.tasks.ImageTransformationTaskGroup;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.utils.TestTransformationTaskGroupCreator;
import com.example.uploads_api.v2.transformations.operations.AspectRatio;
import com.example.uploads_api.v2.transformations.operations.ImageEncodings;
import com.example.uploads_api.v2.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.v2.transformations.operations.LimitResolution;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestApplication.class)
@Import(LocalStorageConfiguration.class)
@TestPropertySource(locations = "classpath:image-transformation-test.properties")
class TaskServiceIT {
    private static final ObjectLocation TEST_FILE = new ObjectLocation("image.jpg", "test-images");
    private static final int ORIGINAL_WIDTH = 1280;
    private static final int ORIGINAL_HEIGHT = 852;
    @Autowired
    private TaskService service;

    @Test
    @SuppressWarnings("SuspiciousNameCombination")
    void aspectFillProducesSquare() throws IOException {
        var task = localTestTask("aspect-fill-square", c -> c.aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.FILL)));
        executeTasks(task);
        assertSize(ORIGINAL_HEIGHT, ORIGINAL_HEIGHT, task);
    }

    @Test
    @SuppressWarnings("SuspiciousNameCombination")
    void aspectContainPadsToSquare() throws IOException {
        var task = localTestTask("aspect-contain-square", c -> c.aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.CONTAIN)));
        executeTasks(task);
        assertSize(ORIGINAL_WIDTH, ORIGINAL_WIDTH, task);
    }

    @Test
    void limitWidthProducesExpectedResolution() throws IOException {
        var task = localTestTask("limit-width-400", c -> c.limitWidth(new LimitResolution(400, LimitResolution.Mode.KEEP_ASPECT_RATIO)));
        executeTasks(task);
        assertSize(400, 266, task);
    }

    @Test
    void quality95Executes() {
        executeTasks(localTestTask("quality-95", c -> c.encoding(
                ImageEncodings.Jpeg.builderWithDefaults().quality(95).build()
        )));
    }

    @Test
    void quality50Executes() {
        executeTasks(localTestTask("quality-50", c -> c.encoding(
                ImageEncodings.Jpeg.builderWithDefaults().quality(50).build()
        )));
    }

    @Test
    void quality50WebpExecutes() {
        executeTasks(localTestTask("format-webp", c -> c.encoding(
                ImageEncodings.Webp.builderWithDefaults().quality(50).build()
        )));
    }

    private void assertSize(
            int expectedWidth,
            int expectedHeight,
            @NonNull ImageTransformationTaskGroup group
    ) throws IOException {
        var tasks = group.tasks();
        var myTask = tasks.stream().toList().getFirst();
        var outputFile = LocalStreamStorage.objectLocationToLocalPath(myTask.outputObject()).toFile();
        var image = ImageIO.read(outputFile);
        assertNotNull(image);
        assertEquals(expectedWidth, image.getWidth());
        assertEquals(expectedHeight, image.getHeight());
    }

    private void executeTasks(
            @NonNull ImageTransformationTaskGroup tasks
    ) {
        service.processTask(tasks);
    }

    private @NonNull ImageTransformationTaskGroup localTestTask(
            @NonNull String name,
            Consumer<ImageTransformationOperations.@NonNull ImageTransformationOperationsBuilder> customizer
    ) {
        var operationsBuilder = ImageTransformationOperations.builderWithDefaults()
                .encoding(ImageEncodings.Jpeg.builderWithDefaults().build());

        if (customizer != null)
            customizer.accept(operationsBuilder);

        var operations = operationsBuilder.build();
        var extension = operations.getFormat().getExtensions()[0];
        var location = new ObjectLocation(name + "." + extension, TEST_FILE.bucket());

        return TestTransformationTaskGroupCreator.createImageTransformationTaskGroup(
                c -> c.inputObject(TEST_FILE)
                        .tasks(
                                List.of(
                                        ImageTransformationTaskGroup.ImageTask.builder()
                                                .name(name)
                                                .outputObject(location)
                                                .lazy(false)
                                                .operations(operations)
                                                .build()
                                )
                        )
        );
    }
}
