package com.example.media_api.transformations;

import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.UploadTransformationOperations;
import com.example.media_api.transformations.operations.VideoTransformationOperations;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.MediaType;
import com.example.media_api.uploads.ObjectPath;
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

    private FileType getOutputFileType() {
        if (operations instanceof ImageTransformationOperations imageOperations) {
            return imageOperations.getFormat();
        }
        if (operations instanceof VideoTransformationOperations videoOperations) {
            return videoOperations.getFormat();
        }
        throw new IllegalArgumentException("Unsupported transformation type");
    }

    public MediaType getMediaType() {
        return getOutputFileType().getMediaType();
    }

    private String getOutputExtension() {
        return getOutputFileType().getExtensions()[0];
    }

    public @NotNull ObjectPath getOutputObject(@NotNull UploadId originalId) {
        var extension = getOutputExtension();
        return new ObjectPath(originalId.get() + "/" + name + "." + extension, outputBucket);
    }
}
