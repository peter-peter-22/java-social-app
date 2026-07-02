package com.example.transformations;

import com.example.transformations.operations.UploadTransformationOperations;
import com.example.uploads.UploadId;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class UploadTransformationDTO {
    private final @NotNull String name;
    private final @NotNull String bucketName;
    private final @NotNull UploadId original;
    private final @Nullable String webhookUrl;
    private final @NotNull UploadTransformationOperations operations;

    public static UploadTransformationDTO toDTO(UploadTransformation transformation, UploadId original) {
        return new UploadTransformationDTO(transformation.getName(), transformation.getBucketName(), original, transformation.getWebhookUrl(), transformation.getOperations());
    }
}
