package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.filters.TransformationFilter;
import com.example.uploads_api.transformations.filters.TransformationFilters;
import com.example.uploads_api.transformations.lazy_transformation_store.LazyTransformationStore;
import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.transformations.sources.VideoTransformationSource;
import com.example.uploads_api.uploads.Upload;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.uploads_api.utils.TestTransformationCreator.createImageTransformation;
import static com.example.uploads_api.utils.TestTransformationCreator.createVideoTransformation;
import static com.example.uploads_api.utils.TestUploadCreator.*;
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

    private TransformationService service;

    @NotNull TransformationService createService(){
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

        var expectedBlockingTask = blockingImageTransformation.createTaskDTO(image);
        var expectedLazyTask = lazyImageTransformation.createTaskDTO(image);
        var exceptedLazyNames = List.of(lazyImageTransformation.getName());

        service.applyTransformations(image);

        verify(blockingTransformationService).transformImages(List.of(expectedBlockingTask));
        verifyNoMoreInteractions(blockingTransformationService);

        verify(lazyTransformationStore).createLazyTransformationSession(image.id(), exceptedLazyNames);
        verify(lazyTransformationService).queueImageTransformations(List.of(expectedLazyTask));
        verifyNoMoreInteractions(lazyTransformationService);
        verifyNoMoreInteractions(lazyTransformationStore);
    }

    /**
     * A blocking and a lazy video transformation should be applied.
     */
    @Test
    void testVideoTransformation() {
        var service = createService();

        var expectedBlockingTask = blockingVideoTransformation.createTaskDTO(video);
        var expectedLazyTask = lazyVideoTransformation.createTaskDTO(video);
        var exceptedLazyNames = List.of(lazyVideoTransformation.getName());

        service.applyTransformations(video);

        verify(blockingTransformationService).transformVideos(List.of(expectedBlockingTask));
        verifyNoMoreInteractions(blockingTransformationService);

        verify(lazyTransformationStore).createLazyTransformationSession(video.id(), exceptedLazyNames);
        verify(lazyTransformationService).queueVideoTransformations(List.of(expectedLazyTask));
        verifyNoMoreInteractions(lazyTransformationService);
        verifyNoMoreInteractions(lazyTransformationStore);
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

        service.applyTransformations(upload);

        verifyNoInteractions(blockingTransformationService);
        verifyNoInteractions(lazyTransformationService);
        verifyNoInteractions(lazyTransformationStore);
    }
}
