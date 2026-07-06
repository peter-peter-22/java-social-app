package com.example.media.image_transformer;

import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.AspectRatio;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.uploads.UploadId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TransformationServiceTests {
    @Autowired
    private TransformationService transformationService;

    @Test
    void testAspectRatio() {
        var upload = new UploadTransformationDTO(
                "example",
                "transformations",
                new UploadId("image.jpg", "uploads"),
                null,
                ImageTransformationOperations.builder()
                        .aspectRatio(new AspectRatio(1,1, AspectRatio.Mode.FILL))
                        .build()
        );

        transformationService.applyTransformations(upload);
    }
}
