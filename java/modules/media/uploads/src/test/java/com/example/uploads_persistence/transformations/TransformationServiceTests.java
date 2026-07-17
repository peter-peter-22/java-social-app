package com.example.uploads_persistence.transformations;

import com.example.media_api.transformations.filters.TransformationFilter;
import com.example.media_api.transformations.filters.TransformationFilters;

import com.example.media_api.transformations.sources.ImageTransformationSource;
import com.example.media_api.transformations.sources.VideoTransformationSource;
import com.example.media_api.uploads.Upload;

import com.example.uploads_persistence.lazy_transformation_session_service.LazyTransformationSessionService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.media_api.utils.TestTransformationCreator.*;

import static com.example.media_api.utils.TestUploadCreator.*;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransformationServiceTests {
    @Mock
    private BlockingTransformationService blockingTransformationService;
    @Mock
    private LazyTransformationService lazyTransformationService;
    @Mock
    private LazyTransformationSessionService lazyTransformationSessionService;

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
                lazyTransformationSessionService
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

        verify(lazyTransformationSessionService).createLazyTransformationSession(image.id(), exceptedLazyNames);
        verify(lazyTransformationService).queueImageTransformations(List.of(expectedLazyTask));
        verifyNoMoreInteractions(lazyTransformationService);
        verifyNoMoreInteractions(lazyTransformationSessionService);
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

        verify(lazyTransformationSessionService).createLazyTransformationSession(video.id(), exceptedLazyNames);
        verify(lazyTransformationService).queueVideoTransformations(List.of(expectedLazyTask));
        verifyNoMoreInteractions(lazyTransformationService);
        verifyNoMoreInteractions(lazyTransformationSessionService);
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
                lazyTransformationSessionService
        );

        service.applyTransformations(upload);

        verifyNoInteractions(blockingTransformationService);
        verifyNoInteractions(lazyTransformationService);
        verifyNoInteractions(lazyTransformationSessionService);
    }
}
