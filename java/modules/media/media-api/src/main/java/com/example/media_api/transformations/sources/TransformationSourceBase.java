package com.example.media_api.transformations.sources;

import com.example.media_api.transformations.filters.TransformationFilter;
import com.example.media_api.uploads.Upload;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuperBuilder
@Getter
public abstract class TransformationSourceBase {
    @NotNull
    final private String name;
    /**
     * If true, the transformation will be executed asynchronously, otherwise it will block the request.
     */
    @Builder.Default
    private final boolean lazy = false;
    @NotNull
    final private String outputBucket;
    @Nullable
    final private TransformationFilter[] filters;

    /**
     * Check if the filters apply to this upload.
     */
    public boolean isApplicable(@NotNull Upload upload) {
        // if no filters are defined, the transformation is always applicable.
        if (filters == null) return true;

        // check if all filters are applicable.
        for (var filter : filters) {
            if (filter != null && !filter.isApplicable(upload)) {
                return false;
            }
        }
        return true;
    }
}
