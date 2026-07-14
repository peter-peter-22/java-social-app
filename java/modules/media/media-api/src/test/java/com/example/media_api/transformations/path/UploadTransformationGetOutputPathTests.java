package com.example.media_api.transformations.path;

import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.ObjectLocation;

import static com.example.media_api.utils.TestTransformationCreator.*;

import com.example.media_api.utils.TestUploadCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UploadTransformationGetOutputPathTests {
    @Test
    void getImageOutputId() {
        var name = "name";
        var outputBucket = "output_bucket";
        var original = TestUploadCreator.createUploadFromLocation(new ObjectLocation("posts/original.123.jpg", "bucket"));
        var transformation = createImageTransformation(
                ops -> ops.name(name).outputBucket(outputBucket).operations(
                        ImageTransformationOperations.builder().format(FileType.WEBP).build()
                )
        );

        var exceptedOutputLocation = new ObjectLocation("bucket/posts/original.123.jpg/name.webp", outputBucket);
        var actualLocation = TransformationPathManager.getOutputObject(original, transformation);

        assertEquals(exceptedOutputLocation, actualLocation);
    }

}
