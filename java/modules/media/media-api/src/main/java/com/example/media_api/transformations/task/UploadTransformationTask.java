package com.example.media_api.transformations.task;

import com.example.media_api.transformations.UploadTransformation;
import com.example.media_api.uploads.UploadId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

@SuperBuilder
@Getter
public class UploadTransformationTask extends UploadTransformation {
    @NotNull UploadId original;

    public static @NotNull UploadTransformationTask fromTransformation(@NotNull UploadTransformation transformation, @NotNull UploadId original) {
        return UploadTransformationTask.builder()
                .name(transformation.getName())
                .outputBucket(transformation.getOutputBucket())
                .operations(transformation.getOperations())
                .lazy(transformation.isLazy())
                .original(original)
                .build();
    }
}
