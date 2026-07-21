package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.filters.TransformationFilter;
import com.example.uploads_api.transformations.filters.TransformationFilters;
import com.example.uploads_api.transformations.lazy_transformation_store.LazyTransformationStore;
import com.example.uploads_api.transformations.mappers.ImageTransformationSourceMapper;
import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.transformations.sources.VideoTransformationSource;
import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.uploads_api.utils.TestTransformationCreator.createImageTransformation;
import static com.example.uploads_api.utils.TestTransformationCreator.createVideoTransformation;
import static com.example.uploads_api.utils.TestUploadCreator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransformationServiceTests {
    @Mock
    private BlockingTransformationService blockingTransformationService;
    @Mock
    private LazyTransformationService lazyTransformationService;
    @Mock
    private LazyTransformationStore lazyTransformationStore;

    private final ImageTransformationSource blockingImageTransformation = createImageTransformation(ops -> ops.lazy(false));
    private final ImageTransformationSource lazyImageTransformation = createImageTransformation(ops -> ops.lazy(true));
    private final VideoTransformationSource blockingVideoTransformation = createVideoTransformation(ops -> ops.lazy(false));
    private final VideoTransformationSource lazyVideoTransformation = createVideoTransformation(ops -> ops.lazy(true));

    private final Upload image = createImage();
    private final Upload video = createVideo();

    @NonNull TransformationService createService() {
        return new TransformationService(
                blockingTransformationService,
                lazyTransformationService,
                List.of(blockingImageTransformation, lazyImageTransformation),
                List.of(blockingVideoTransformation, lazyVideoTransformation),
                lazyTransformationStore
        );
    }

    /**
     * A blocking and a lazy image transformation should be applied.
     */
    @Test
    void testImageTransformation() {
        var service = createService();

        var expectedBlockingTask = imageTaskGroup(blockingImageTransformation);
        var expectedLazyTask = imageTaskGroup(lazyImageTransformation);
        var exceptedLazyNames = List.of(lazyImageTransformation.getName());

        var hasLazy = service.applyTransformations(image);

        assertThat(hasLazy).isTrue();

        verify(blockingTransformationService).transformImages(expectedBlockingTask);
        verifyNoMoreInteractions(blockingTransformationService);

        verify(lazyTransformationStore).createLazyTransformationSession(image.id(), exceptedLazyNames);
        verify(lazyTransformationService).queueImageTransformations(expectedLazyTask);
        verifyNoMoreInteractions(lazyTransformationService);
        verifyNoMoreInteractions(lazyTransformationStore);
    }

    /**
     * A blocking and a lazy video transformation should be applied.
     */
    @Test
    void testVideoTransformation() {
        // implement later
    }

    /**
     * No transformations should be created if the filter is not applicable
     */
    @Test
    void testFiltering() {
        var simpleFilter = new TransformationFilter[]{new TransformationFilters.BucketFilter("pass")};
        var upload = createUploadFromBucket("fail");
        var service = new TransformationService(
                blockingTransformationService,
                lazyTransformationService,
                List.of(createImageTransformation(ops -> ops.filters(simpleFilter))),
                List.of(createVideoTransformation()),
                lazyTransformationStore
        );

        var hasLazy = service.applyTransformations(upload);

        assertThat(hasLazy).isFalse();

        verifyNoInteractions(blockingTransformationService);
        verifyNoInteractions(lazyTransformationService);
        verifyNoInteractions(lazyTransformationStore);
    }

    /**
     * No lazy transformations should be detected if there are only blocking transformations.
     */
    @Test
    void testBlockingOnly() {
        var service = new TransformationService(
                blockingTransformationService,
                lazyTransformationService,
                List.of(blockingImageTransformation),
                List.of(),
                lazyTransformationStore
        );

        var expectedBlockingTask = imageTaskGroup(blockingImageTransformation);

        var hasLazy = service.applyTransformations(image);

        assertThat(hasLazy).isFalse();

        verify(blockingTransformationService).transformImages(expectedBlockingTask);
        verifyNoMoreInteractions(blockingTransformationService);

        verifyNoInteractions(lazyTransformationService);
        verifyNoInteractions(lazyTransformationStore);
    }

    private ImageTransformationTaskGroupDTO imageTaskGroup(ImageTransformationSource transformation) {
        return new ImageTransformationTaskGroupDTO(
                image.objectLocation(),
                List.of(ImageTransformationSourceMapper.createTaskDTO(transformation, image))
        );
    }
}
