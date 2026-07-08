package com.example.media_api.transformations;

import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.Upload;
import com.example.media_api.uploads.UploadId;
import com.example.media_api.uploads.UploadStatus;
import com.example.users.api.repository.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TransformationFiltersTests {

    private Upload createUploadFromPath(String path){
        return new Upload(
                new UploadId(path,"bucket"),
                new UserId(UUID.randomUUID()),
                FileType.JPEG,
                Instant.now(),
                UploadStatus.READY
        );
    }

    private Upload createUploadFromBucket(String bucket){
        return new Upload(
                new UploadId("file.jpg",bucket),
                new UserId(UUID.randomUUID()),
                FileType.JPEG,
                Instant.now(),
                UploadStatus.READY
        );
    }

    /** The path prefix filter should apply only if the path starts with the prefix */
    @Test
    void testPathPrefix() {
        record TestCase(String prefix, String[] shouldApply, String[] shouldNotApply) {
        }

        var testCases = new TestCase[]{
                new TestCase(
                        "a",
                        new String[]{"a/b/c"},
                        new String[]{"d/e/f"}
                ),
                new TestCase(
                        "posts/",
                        new String[]{
                                "posts/1212-2112-131.jpg",
                                "posts/images/1.jpg",
                        },
                        new String[]{
                                "posts_v2/1212-2112-131.jpg",
                                "/posts/images/1.jpg",
                                "avatars/1.jpg"
                        }
                )
        };

        for (var testCase : testCases) {
            var filter = new TransformationFilters.PathPrefix(testCase.prefix);
            for (var path : testCase.shouldApply()) {
                var upload = createUploadFromPath(path);
                assertThat(filter.isApplicable(upload)).isTrue();
            }
            for (var path : testCase.shouldNotApply()) {
                var upload = createUploadFromPath(path);
                assertThat(filter.isApplicable(upload)).isFalse();
            }
        }
    }

    /** The bucket filter should apply only if the bucket matches the given name */
    @Test
    void testBucket(){
        // create the filter
        var filter = new TransformationFilters.BucketFilter("my bucket");

        // create uploads
        var matches = createUploadFromBucket("my bucket");
        var doesNotMatch = createUploadFromBucket("other");

        // check filtering
        assertThat(filter.isApplicable(matches)).isTrue();
        assertThat(filter.isApplicable(doesNotMatch)).isFalse();
    }
}
