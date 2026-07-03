package com.example.media.common.transformations;

import com.example.media.common.transformations.operations.ImageTransformationOperations;
import com.example.media.common.transformations.operations.UploadTransformationOperations;
import com.example.media.common.transformations.operations.VideoTransformationOperations;
import com.example.media.common.uploads.MediaType;
import com.example.media.common.uploads.Upload;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuperBuilder
@Getter
public class UploadTransformation {
    @NotNull final private String name;
    @Builder.Default private final boolean lazy = false;
    @Nullable final private String webhookUrl;
    @NotNull final private String bucketName;
    @Getter(AccessLevel.NONE) @NotNull final private TransformationFilter @Nullable [] filters;
    @NotNull final private UploadTransformationOperations operations;

    public MediaType getMediaType(){
        if (operations instanceof ImageTransformationOperations) return MediaType.IMAGE;
        else if (operations instanceof VideoTransformationOperations) return MediaType.VIDEO;
        else throw new IllegalArgumentException("Unsupported transformation type");
    }

    /** Check if the filters apply to this upload. */
    public boolean isApplicable(@NotNull Upload upload) {
        // if no filters are defined, the transformation is always applicable.
        if (filters == null) return true;

        // check if the operations are compatible with the file upload
        if (!getMediaType().equals(upload.fileType().mediaType())) return false;

        // check if all filters are applicable.
        for (var filter : filters) {
            if (!filter.isApplicable(upload)) {
                return false;
            }
        }
        return true;
    }
}
