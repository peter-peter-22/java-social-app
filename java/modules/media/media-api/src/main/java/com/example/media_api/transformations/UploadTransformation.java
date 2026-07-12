package com.example.media_api.transformations;

import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.UploadTransformationOperations;
import com.example.media_api.transformations.operations.VideoTransformationOperations;
import com.example.media_api.transformations.source.TransformationFilter;
import com.example.media_api.uploads.MediaType;
import com.example.media_api.uploads.Upload;
import com.example.media_api.uploads.UploadId;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

@SuperBuilder
@Getter
public class UploadTransformation {
    @NotNull
    final private String name;
    /**
     * If true, the transformation will be executed asynchronously, otherwise it will block the request.
     */
    @Builder.Default
    private final boolean lazy = false;
    @NotNull
    final private String outputBucket;
    @NotNull
    final private UploadTransformationOperations operations;

    public MediaType getMediaType() {
        if (operations instanceof ImageTransformationOperations) return MediaType.IMAGE;
        else if (operations instanceof VideoTransformationOperations) return MediaType.VIDEO;
        else throw new IllegalArgumentException("Unsupported transformation type");
    }

    public @NotNull UploadId getOutputId(@NotNull UploadId originalId) {
        var extension = originalId.objectPath().substring(originalId.objectPath().lastIndexOf(".") + 1);
        return new UploadId(originalId.bucket() + "/" + originalId.objectPath() + "/" + name + "." + extension, outputBucket);
    }
}
