package com.example.media.uploads.transformations;

import com.example.media.api.transformations.api.UploadTransformationDTO;
import com.example.media.api.transformations.operations.ImageTransformationOperations;
import com.example.media.api.transformations.operations.UploadTransformationOperations;
import com.example.media.api.transformations.operations.VideoTransformationOperations;
import com.example.media.api.uploads.MediaType;
import com.example.media.api.uploads.UploadId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

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

        List<UploadTransformationDTO> imageTransformations = List.of(
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

        List<UploadTransformationDTO> videoTransformations = List.of(
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

        List<UploadTransformationDTO> transformations = List.of(
                transformation(MediaType.IMAGE),
                transformation(MediaType.VIDEO),
                transformation(MediaType.IMAGE),
                transformation(MediaType.VIDEO)
        );

        List<UploadTransformationDTO> imageTransformations = transformations.stream()
                .filter(t -> t.getMediaType().equals(MediaType.IMAGE))
                .toList();

        List<UploadTransformationDTO> videoTransformations = transformations.stream()
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

        List<UploadTransformationDTO> emptyTransformations = List.of();

        // Act & Assert
        assertDoesNotThrow(() -> selector.transform(emptyTransformations));

        verify(mockImageApi, never()).transform(anyCollection());
        verify(mockVideoApi, never()).transform(anyCollection());
    }

    private UploadTransformationDTO transformation(MediaType mediaType) {
        UploadTransformationOperations operations = switch (mediaType) {
            case IMAGE -> ImageTransformationOperations.builder().build();
            case VIDEO -> VideoTransformationOperations.builder().build();
        };

        return new UploadTransformationDTO(
                mediaType.name().toLowerCase(),
                "transformations",
                new UploadId("original", "uploads"),
                null,
                operations
        );
    }
}
