package com.example.uploads.transformations;

import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.UploadTransformationOperations;
import com.example.media_api.transformations.operations.VideoTransformationOperations;
import com.example.media_api.transformations.source.UploadTransformationSource;
import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.media_api.uploads.MediaType;
import com.example.media_api.uploads.UploadId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

// CLEANING: this seems long
class TransformationApiSelectorTest {

    /**
     * Test class for the {@code TransformationApiSelector.transform} method.
     * The method filters the transformations by their media type (IMAGE or VIDEO)
     * and delegates processing to the respective APIs (Image or Video).
     * This test class ensures that the delegation logic is functioning correctly.
     */

    @Test
    void testTransformWithOnlyImages() {
        // Arrange
        TransformationApi mockImageApi = mock(TransformationApi.class);
        TransformationApi mockVideoApi = mock(TransformationApi.class);

        TransformationApiSelector selector = new TransformationApiSelector() {
            @Override
            @NotNull
            TransformationApi getImageApi() {
                return mockImageApi;
            }

            @Override
            @NotNull
            TransformationApi getVideoApi() {
                return mockVideoApi;
            }
        };

        List<UploadTransformationTask> imageTransformations = List.of(
                transformation(MediaType.IMAGE),
                transformation(MediaType.IMAGE)
        );

        // Act & Assert
        assertDoesNotThrow(() -> selector.transform(imageTransformations));

        verify(mockImageApi, times(1)).transform(imageTransformations);
        verify(mockVideoApi, never()).transform(anyCollection());
    }

    @Test
    void testTransformWithOnlyVideos() {
        // Arrange
        TransformationApi mockImageApi = mock(TransformationApi.class);
        TransformationApi mockVideoApi = mock(TransformationApi.class);

        TransformationApiSelector selector = new TransformationApiSelector() {
            @Override
            @NotNull
            TransformationApi getImageApi() {
                return mockImageApi;
            }

            @Override
            @NotNull
            TransformationApi getVideoApi() {
                return mockVideoApi;
            }
        };

        List<UploadTransformationTask> videoTransformations = List.of(
                transformation(MediaType.VIDEO),
                transformation(MediaType.VIDEO)
        );

        // Act & Assert
        assertDoesNotThrow(() -> selector.transform(videoTransformations));

        verify(mockVideoApi, times(1)).transform(videoTransformations);
        verify(mockImageApi, never()).transform(anyCollection());
    }

    @Test
    void testTransformWithMixedTransformations() {
        // Arrange
        TransformationApi mockImageApi = mock(TransformationApi.class);
        TransformationApi mockVideoApi = mock(TransformationApi.class);

        TransformationApiSelector selector = new TransformationApiSelector() {
            @Override
            @NotNull
            TransformationApi getImageApi() {
                return mockImageApi;
            }

            @Override
            @NotNull
            TransformationApi getVideoApi() {
                return mockVideoApi;
            }
        };

        List<UploadTransformationTask> transformations = List.of(
                transformation(MediaType.IMAGE),
                transformation(MediaType.VIDEO),
                transformation(MediaType.IMAGE),
                transformation(MediaType.VIDEO)
        );

        List<UploadTransformationTask> imageTransformations = transformations.stream()
                .filter(t -> t.getMediaType().equals(MediaType.IMAGE))
                .toList();

        List<UploadTransformationTask> videoTransformations = transformations.stream()
                .filter(t -> t.getMediaType().equals(MediaType.VIDEO))
                .toList();

        // Act & Assert
        assertDoesNotThrow(() -> selector.transform(transformations));

        verify(mockImageApi, times(1)).transform(imageTransformations);
        verify(mockVideoApi, times(1)).transform(videoTransformations);
    }

    @Test
    void testTransformWithEmptyTransformations() {
        // Arrange
        TransformationApi mockImageApi = mock(TransformationApi.class);
        TransformationApi mockVideoApi = mock(TransformationApi.class);

        TransformationApiSelector selector = new TransformationApiSelector() {
            @Override
            @NotNull
            TransformationApi getImageApi() {
                return mockImageApi;
            }

            @Override
            @NotNull
            TransformationApi getVideoApi() {
                return mockVideoApi;
            }
        };

        List<UploadTransformationTask> emptyTransformations = List.of();

        // Act & Assert
        assertDoesNotThrow(() -> selector.transform(emptyTransformations));

        verify(mockImageApi, never()).transform(anyCollection());
        verify(mockVideoApi, never()).transform(anyCollection());
    }

    private UploadTransformationTask transformation(MediaType mediaType) {
        UploadTransformationOperations operations = switch (mediaType) {
            case IMAGE -> ImageTransformationOperations.builder().build();
            case VIDEO -> VideoTransformationOperations.builder().build();
        };

        return UploadTransformationTask.builder()
                .original(new UploadId("file.jpg", "uploads"))
                .outputBucket("transformations")
                .operations(operations)
                .build();
    }
}
