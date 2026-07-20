package com.example.uploads_api.transformations.filters;

import com.example.uploads_api.uploads.Upload;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

public class TransformationFilters {

    @AllArgsConstructor
    public static class KeyPrefix implements TransformationFilter {
        private final String prefix;

        @Override
        public boolean isApplicable(@NonNull Upload upload) {
            return upload.objectLocation().key().startsWith(prefix);
        }
    }

    @AllArgsConstructor
    public static class BucketFilter implements TransformationFilter {
        private final String bucket;

        @Override
        public boolean isApplicable(@NonNull Upload upload) {
            return upload.objectLocation().bucket().equals(bucket);
        }
    }

}
