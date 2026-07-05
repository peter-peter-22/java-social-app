package com.example.media.common.transformations.api;

import com.example.media.common.transformations.UploadTransformation;
import com.example.media.common.transformations.operations.ImageTransformationOperations;
import com.example.media.common.transformations.operations.UploadTransformationOperations;
import com.example.media.common.transformations.operations.VideoTransformationOperations;
import com.example.media.common.uploads.MediaType;
import com.example.media.common.uploads.UploadId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record UploadTransformationDTO(
        @NotNull String name,
        @NotNull String outputBucket,
        @NotNull UploadId original,
        @Nullable String webhookUrl,
        @NotNull UploadTransformationOperations operations
) {
    public static UploadTransformationDTO toDTO(UploadTransformation transformation, UploadId original) {
        return new UploadTransformationDTO(
                transformation.getName(),
                transformation.getOutputBucket(),
                original, transformation.getWebhookUrl(),
                transformation.getOperations()
        );
    }

    public MediaType getMediaType(){
        if (operations instanceof ImageTransformationOperations) return MediaType.IMAGE;
        else if (operations instanceof VideoTransformationOperations) return MediaType.VIDEO;
        else throw new IllegalArgumentException("Unsupported transformation type");
    }
}
