package com.example.media.uploads.transformations;

import com.example.media_api.uploads.UploadId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransformationServiceMarkAsReadyTests {
    @Mock
    private LazyTransformationSessionRepository repository;

    @InjectMocks
    private TransformationService service;

    /** The session should be updated but not deleted */
    @Test
    void testMarkAsReadyIncomplete() {
        var uploadId=new UploadId("image.jpg","bucket");
        var transformationName="test";

        // not ready yet
        when(repository.markLazyTransformationAsReady(any(),any())).thenReturn(false);

        service.markLazyTransformationAsComplete(uploadId,transformationName);

        verify(repository).markLazyTransformationAsReady(uploadId,transformationName);
        verifyNoMoreInteractions(repository);
    }

    /** The session should be updated and deleted */
    @Test
    void testMarkAsReadyComplete() {
        var uploadId=new UploadId("image.jpg","bucket");
        var transformationName="test";

        // ready, delete session
        when(repository.markLazyTransformationAsReady(any(),any())).thenReturn(true);

        service.markLazyTransformationAsComplete(uploadId,transformationName);

        verify(repository).markLazyTransformationAsReady(uploadId,transformationName);
        verify(repository).deleteLazyTransformationSession(uploadId);
    }
}
