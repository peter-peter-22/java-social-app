package com.example.media_api.transformations;

import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.uploads.UploadId;
import org.junit.jupiter.api.Test;

public class UploadTransformationGetOutputIdTests {
    /** The output ID should be generated as excepted */
    @Test
    void getOutputId(){
        var name="name";
        var outputBucket="output_bucket";
        var original=new UploadId("posts/original.123.jpg", "bucket");

        var transformation=UploadTransformation.builder()
                .name(name)
                .outputBucket(outputBucket)
                .operations(ImageTransformationOperations.builder().build())
                .build();

        var outputId=transformation.getOutputId(original);
        var exceptedOutputId=new UploadId("bucket/posts/original.123.jpg/name.jpg", outputBucket);

        assert(outputId.equals(exceptedOutputId));
    }
}
