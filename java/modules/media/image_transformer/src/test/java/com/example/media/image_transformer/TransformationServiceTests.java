package com.example.media.image_transformer;

import com.example.media.common.transformations.api.UploadTransformationDTO;
import com.example.media.common.transformations.operations.ImageTransformationOperations;
import com.example.media.common.transformations.operations.LimitResolution;
import com.example.media.common.uploads.UploadId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TransformationServiceTests {
    @Autowired
    private TransformationService transformationService;

    @Test
    void testThumbnail() {
        var upload = new UploadTransformationDTO(
                "example",
                "transformations",
                new UploadId("image.jpg", "uploads"),
                null,
                ImageTransformationOperations.builder()
                        .limitHeight(new LimitResolution(100, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                        .limitWidth(new LimitResolution(100, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                        .build()
        );

        transformationService.ApplyTransformations(upload);
    }
}
