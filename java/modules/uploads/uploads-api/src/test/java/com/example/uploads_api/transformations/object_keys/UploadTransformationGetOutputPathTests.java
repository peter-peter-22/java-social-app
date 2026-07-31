package com.example.uploads_api.transformations.object_keys;

import com.example.uploads_api.transformations.operations.ImageEncodings;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.utils.TestUploadCreator;
import org.junit.jupiter.api.Test;

import static com.example.uploads_api.utils.TestTransformationSourceCreator.createImageTransformation;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UploadTransformationGetOutputPathTests {
    @Test
    void getImageOutputId() {
        var name = "name";
        var outputBucket = "output_bucket";
        var original = TestUploadCreator.createUpload(c -> c.objectLocation(new ObjectLocation("posts/original.123.jpg", "bucket")));
        var transformation = createImageTransformation(
                ops -> ops.name(name).outputBucket(outputBucket).operations(
                        ImageTransformationOperations.builder()
                                .encoding(
                                        ImageEncodings.Webp.builderWithDefaults().build()
                                )
                                .build()
                )
        );

        var exceptedOutputLocation = new ObjectLocation("bucket/posts/original.123.jpg/name.webp", outputBucket);
        var actualLocation = TransformationKeyManager.getOutputObject(original, transformation);

        assertEquals(exceptedOutputLocation, actualLocation);
    }

}
