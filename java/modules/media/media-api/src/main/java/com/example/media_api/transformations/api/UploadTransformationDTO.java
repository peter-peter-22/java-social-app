package com.example.media_api.transformations.api;

import com.example.media_api.transformations.UploadTransformation;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.UploadTransformationOperations;
import com.example.media_api.transformations.operations.VideoTransformationOperations;
import com.example.media_api.uploads.MediaType;
import com.example.media_api.uploads.UploadId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record UploadTransformationDTO(
        @NotNull String name,
        @NotNull String outputBucket,
        @NotNull UploadId original,
        @NotNull Boolean webhook,
        @NotNull UploadTransformationOperations operations
) {
    public static UploadTransformationDTO toDTO(UploadTransformation transformation, UploadId original) {
        return new UploadTransformationDTO(
                transformation.getName(),
                transformation.getOutputBucket(),
                original, transformation.isLazy(),
                transformation.getOperations()
        );
    }

    public MediaType getMediaType(){
        if (operations instanceof ImageTransformationOperations) return MediaType.IMAGE;
        else if (operations instanceof VideoTransformationOperations) return MediaType.VIDEO;
        else throw new IllegalArgumentException("Unsupported transformation type");
    }
}
