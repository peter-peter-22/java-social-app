package com.example.media_api.transformations;

import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.VideoTransformationOperations;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.Upload;
import com.example.media_api.uploads.UploadId;
import com.example.media_api.uploads.UploadStatus;
import com.example.users.api.repository.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UploadTransformationTests {
    private Upload createUploadFromFileType(FileType fileType) {
        return new Upload(
                new UploadId("1.jpg", "bucket"),
                new UserId(UUID.randomUUID()),
                fileType,
                Instant.now(),
                UploadStatus.READY
        );
    }

    /**
     * The transformation should only apply to uploads those match the operation's media type
     */
    @Test
    void testMediaTypeFiltering() {
        // create transformations
        var imageTransformation = UploadTransformation.builder()
                .name("images")
                .outputBucket("bucket")
                .operations(ImageTransformationOperations.builder().build())
                .build();

        var videoTransformation = UploadTransformation.builder()
                .name("videos")
                .outputBucket("bucket")
                .operations(VideoTransformationOperations.builder().build())
                .build();

        // create uploads
        var imageUpload = createUploadFromFileType(FileType.JPEG);
        var videoUpload = createUploadFromFileType(FileType.MP4);

        // check image filtering
        assertThat(imageTransformation.isApplicable(imageUpload)).isTrue();
        assertThat(imageTransformation.isApplicable(videoUpload)).isFalse();

        // check video filtering
        assertThat(videoTransformation.isApplicable(imageUpload)).isFalse();
        assertThat(videoTransformation.isApplicable(videoUpload)).isTrue();
    }

    /** The transformation should only apply to uploads that match all filters */
    @Test
    void testFilterCombination() {
        // create transformation
        var transformation = UploadTransformation.builder()
                .name("images")
                .outputBucket("bucket")
                .filters(new TransformationFilter[]{
                        new TransformationFilters.PathPrefix("avatars/"),
                        new TransformationFilters.BucketFilter("uploads")
                })
                .operations(ImageTransformationOperations.builder().build())
                .build();

        // create uploads
        var bucketMatches = new Upload(
                new UploadId("posts/1.jpg", "uploads"),
                new UserId(UUID.randomUUID()),
                FileType.JPEG,
                Instant.now(),
                UploadStatus.READY
        );

        var pathMatches = new Upload(
                new UploadId("avatars/1.jpg", "private"),
                new UserId(UUID.randomUUID()),
                FileType.JPEG,
                Instant.now(),
                UploadStatus.READY
        );

        var bothMatches = new Upload(
                new UploadId("avatars/1.jpg", "uploads"),
                new UserId(UUID.randomUUID()),
                FileType.JPEG,
                Instant.now(),
                UploadStatus.READY
        );

        // check filtering
        assertThat(transformation.isApplicable(bucketMatches)).isFalse();
        assertThat(transformation.isApplicable(pathMatches)).isFalse();
        assertThat(transformation.isApplicable(bothMatches)).isTrue();
    }
}
