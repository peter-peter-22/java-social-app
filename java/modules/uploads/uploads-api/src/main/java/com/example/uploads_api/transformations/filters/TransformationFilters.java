package com.example.uploads_api.transformations.filters;

import com.example.uploads_api.uploads.Upload;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class TransformationFilters {

    @AllArgsConstructor
    public static class PathPrefix implements TransformationFilter {
        private final String prefix;

        @Override
        public boolean isApplicable(@NotNull Upload upload) {
            return upload.objectLocation().path().startsWith(prefix);
        }
    }

    @AllArgsConstructor
    public static class BucketFilter implements TransformationFilter {
        private final String bucket;

        @Override
        public boolean isApplicable(@NotNull Upload upload) {
            return upload.objectLocation().bucket().equals(bucket);
        }
    }

}
