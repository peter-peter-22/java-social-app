package com.example.uploads_persistence.transformations;

import com.example.uploads_persistence.lazy_transformation_session_service.LazyTransformationSessionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LazyTransformationSessionServiceTests {
    @Mock
    private LazyTransformationSessionService repository;

    @InjectMocks
    private TransformationService service;

    /** The session should be updated but not deleted */
    // TODO implement after the service is done
//    @Test
//    void testMarkAsReadyIncomplete() {
//        var uploadId=new UploadId(UUID.randomUUID());
//        var transformationName="test";
//
//        // not ready yet
//        when(repository.markLazyTransformationAsReady(any(),any())).thenReturn(false);
//
//        service.markLazyTransformationAsComplete(uploadId,transformationName);
//
//        verify(repository).markLazyTransformationAsReady(uploadId,transformationName);
//        verifyNoMoreInteractions(repository);
//    }
//
//    /** The session should be updated and deleted */
//    @Test
//    void testMarkAsReadyComplete() {
//        var uploadId=new UploadId(UUID.randomUUID());
//        var transformationName="test";
//
//        // ready, delete session
//        when(repository.markLazyTransformationAsReady(any(),any())).thenReturn(true);
//
//        service.markLazyTransformationAsComplete(uploadId,transformationName);
//
//        verify(repository).markLazyTransformationAsReady(uploadId,transformationName);
//        verify(repository).deleteLazyTransformationSession(uploadId);
//    }
}
