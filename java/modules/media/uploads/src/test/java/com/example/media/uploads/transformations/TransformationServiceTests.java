package com.example.media.uploads.transformations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TransformationServiceTests {

    @Mock
    private BlockingTransformationRepository blockingTransformationRepository;

    @Mock
    private LazyTransformationSessionRepository lazyTransformationSessionRepository;

    @Mock
    private LazyTransformationEventRepository lazyTransformationEventRepository;

    @InjectMocks
    private TransformationService transformationService;

    @Test
    void testApplyTransformations(){

    }

    @Test
    void testMarkingLazyTransformationAsCompleted(){

    }
}
