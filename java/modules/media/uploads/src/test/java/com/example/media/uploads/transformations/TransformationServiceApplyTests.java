package com.example.media.uploads.transformations;

import com.example.media_api.transformations.TransformationFilter;
import com.example.media_api.transformations.TransformationFilters;
import com.example.media_api.transformations.UploadTransformation;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.AspectRatio;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.LimitResolution;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.Upload;
import com.example.media_api.uploads.UploadId;
import com.example.media_api.uploads.UploadStatus;
import com.example.users.api.repository.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransformationServiceApplyTests {

    @Mock
    private BlockingTransformationApi blockingTransformationApi;
    @Mock
    private LazyTransformationApi lazyTransformationApi;
    @Mock
    private LazyTransformationSessionRepository lazyTransformationSessionRepository;

    private final ImageTransformationOperations exampleOperations = ImageTransformationOperations.builder()
            .aspectRatio(new AspectRatio(1, 2, AspectRatio.Mode.FILL))
            .format(FileType.JPEG)
            .quality(90)
            .limitHeight(new LimitResolution(1080, LimitResolution.Mode.KEEP_ASPECT_RATIO))
            .limitWidth(new LimitResolution(1920, LimitResolution.Mode.KEEP_ASPECT_RATIO))
            .build();

    private final Upload exampleUpload = new Upload(
            new UploadId("posts/1.jpg", "public"),
            new UserId(UUID.randomUUID()),
            FileType.JPEG,
            Instant.now(),
            UploadStatus.UPLOADING
    );

    /**
     * For a lazy transformation, the lazy api and the session repo should be called, the blocking api does not
     */
    @Test
    void testLazyTransformation() {
        var transformation = UploadTransformation.builder()
                .name("lazy")
                .lazy(true)
                .outputBucket("bucket")
                .operations(exampleOperations)
                .build();

        var service = new TransformationService(
                lazyTransformationSessionRepository,
                lazyTransformationApi,
                blockingTransformationApi,
                List.of(transformation)
        );

        var dto= UploadTransformationDTO.toDTO(transformation, exampleUpload.id());

        service.applyTransformations(exampleUpload);

        verify(lazyTransformationApi).transform(List.of(dto));
        verify(lazyTransformationSessionRepository).createLazyTransformationSession(exampleUpload.id(), List.of(transformation.getName()));
        verifyNoInteractions(blockingTransformationApi);
    }

    /**
     * For a blocking transformation, the blocking api should be called, the lazy api and the session repo does not
     */
    @Test
    void testBlockingTransformation() {
        var transformation = UploadTransformation.builder()
                .name("blocking")
                .outputBucket("bucket")
                .operations(exampleOperations)
                .build();

        var service = new TransformationService(
                lazyTransformationSessionRepository,
                lazyTransformationApi,
                blockingTransformationApi,
                List.of(transformation)
        );

        var dto=UploadTransformationDTO.toDTO(transformation, exampleUpload.id());

        service.applyTransformations(exampleUpload);

        verify(blockingTransformationApi).transform(List.of(dto));
        verifyNoInteractions(lazyTransformationSessionRepository);
        verifyNoInteractions(lazyTransformationApi);
    }

    /** No transformations should be created if the filter is not applicable */
    @Test
    void testFiltering() {
        var simpleFilter = new TransformationFilter[]{new TransformationFilters.BucketFilter("pass")};

        var shouldFail= new Upload(
                new UploadId("posts/1.jpg", "fail"),
                new UserId(UUID.randomUUID()),
                FileType.JPEG,
                Instant.now(),
                UploadStatus.UPLOADING
        );

        var transformation = UploadTransformation.builder()
                .name("test")
                .outputBucket("bucket")
                .filters(simpleFilter)
                .operations(exampleOperations)
                .build();

        var service = new TransformationService(
                lazyTransformationSessionRepository,
                lazyTransformationApi,
                blockingTransformationApi,
                List.of(transformation)
        );

        service.applyTransformations(shouldFail);
        verifyNoInteractions(blockingTransformationApi);
        verifyNoInteractions(lazyTransformationApi);
        verifyNoInteractions(lazyTransformationSessionRepository);
    }
}
