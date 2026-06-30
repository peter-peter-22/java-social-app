package com.example.transformations;

import com.example.uploads.Upload;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class TransformationFilters {

    @AllArgsConstructor
    public static class PathPrefix implements TransformationFilter {
        private final String prefix;

        @Override
        public boolean isApplicable(@NotNull Upload upload) {
            return false;
        }
    }

}
