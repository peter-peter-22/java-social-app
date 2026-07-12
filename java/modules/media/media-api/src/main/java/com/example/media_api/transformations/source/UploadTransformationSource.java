package com.example.media_api.transformations.source;

import com.example.media_api.transformations.UploadTransformation;
import com.example.media_api.uploads.Upload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuperBuilder
@Getter
public class UploadTransformationSource extends UploadTransformation {
    @Nullable
    final private TransformationFilter[] filters;

    /**
     * Check if the filters apply to this upload.
     */
    public boolean isApplicable(@NotNull Upload upload) {
        // if no filters are defined, the transformation is always applicable.
        if (filters == null) return true;

        // check if the operations are compatible with the file upload
        if (!getMediaType().equals(upload.fileType().getMediaType())) return false;

        // check if all filters are applicable.
        for (var filter : filters) {
            if (filter != null && !filter.isApplicable(upload)) {
                return false;
            }
        }
        return true;
    }
}
