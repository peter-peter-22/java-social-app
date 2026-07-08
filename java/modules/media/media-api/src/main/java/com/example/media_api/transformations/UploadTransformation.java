package com.example.media_api.transformations;

import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.UploadTransformationOperations;
import com.example.media_api.transformations.operations.VideoTransformationOperations;
import com.example.media_api.uploads.MediaType;
import com.example.media_api.uploads.Upload;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuperBuilder
@Getter
public class UploadTransformation {
    @NotNull final private String name;
    /** If true, the transformation will be executed asynchronously, otherwise it will block the request. */
    @Builder.Default private final boolean lazy = false;
    /** Called after the transformation is completed, works only with lazy transformations. */
    @Nullable final private String webhookUrl;
    @NotNull final private String outputBucket;
    @Builder.Default @NotNull final private TransformationFilter[] filters = new TransformationFilter[0];
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
        if (!getMediaType().equals(upload.fileType().getMediaType())) return false;

        // check if all filters are applicable.
        for (var filter : filters) {
            if (!filter.isApplicable(upload)) {
                return false;
            }
        }
        return true;
    }
}
