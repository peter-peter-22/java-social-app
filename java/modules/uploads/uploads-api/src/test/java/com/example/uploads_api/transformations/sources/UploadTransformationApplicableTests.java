package com.example.uploads_api.transformations.sources;

import com.example.uploads_api.transformations.filters.TransformationFilter;
import com.example.uploads_api.transformations.filters.TransformationFilters;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.utils.TestUploadCreator;
import org.junit.jupiter.api.Test;

import static com.example.uploads_api.utils.TestTransformationCreator.createImageTransformation;
import static com.example.uploads_api.utils.TestTransformationCreator.createVideoTransformation;
import static org.assertj.core.api.Assertions.assertThat;

public class UploadTransformationApplicableTests {

    /**
     * The transformation should only apply to uploads those match the operation's media type
     */
    @Test
    void testMediaTypeFiltering() {
        // create transformations
        var imageTransformation = createImageTransformation();
        var videoTransformation = createVideoTransformation();

        // create uploads
        var imageUpload = TestUploadCreator.createImage();
        var videoUpload = TestUploadCreator.createVideo();

        // check image filtering
        assertThat(imageTransformation.isApplicable(imageUpload)).isTrue();
        assertThat(imageTransformation.isApplicable(videoUpload)).isFalse();

        // check video filtering
        assertThat(videoTransformation.isApplicable(imageUpload)).isFalse();
        assertThat(videoTransformation.isApplicable(videoUpload)).isTrue();
    }

    /**
     * The transformation should only apply to uploads that match all filters
     */
    @Test
    void testFilterCombination() {
        // create transformation
        var transformation = createImageTransformation(
                ops -> ops.filters(new TransformationFilter[]{
                        new TransformationFilters.PathPrefix("avatars/"),
                        new TransformationFilters.BucketFilter("uploads")
                })
        );

        // create uploads
        var bucketMatches = TestUploadCreator.createUpload(c -> c.objectLocation(new ObjectLocation("posts/1.jpg", "uploads")));
        var pathMatches = TestUploadCreator.createUpload(c -> c.objectLocation(new ObjectLocation("avatars/1.jpg", "others")));
        var bothMatches = TestUploadCreator.createUpload(c -> c.objectLocation(new ObjectLocation("avatars/1.jpg", "uploads")));

        // check filtering
        assertThat(transformation.isApplicable(bucketMatches)).isFalse();
        assertThat(transformation.isApplicable(pathMatches)).isFalse();
        assertThat(transformation.isApplicable(bothMatches)).isTrue();
    }
}
