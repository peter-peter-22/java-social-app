package com.example.uploads_api.transformations.filters;

import com.example.uploads_api.utils.TestUploadCreator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransformationFiltersTests {

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
            var filter = new TransformationFilters.KeyPrefix(testCase.prefix);
            for (var path : testCase.shouldApply()) {
                var upload = TestUploadCreator.createUploadFromPath(path);
                assertThat(filter.isApplicable(upload)).isTrue();
            }
            for (var path : testCase.shouldNotApply()) {
                var upload = TestUploadCreator.createUploadFromPath(path);
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
        var matches = TestUploadCreator.createUploadFromBucket("my bucket");
        var doesNotMatch = TestUploadCreator.createUploadFromBucket("other");

        // check filtering
        assertThat(filter.isApplicable(matches)).isTrue();
        assertThat(filter.isApplicable(doesNotMatch)).isFalse();
    }
}
