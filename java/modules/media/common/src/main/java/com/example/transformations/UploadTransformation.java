package com.example.transformations;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuperBuilder
public class UploadTransformation {
    @NotNull final private String name;
    @Builder.Default
    private final boolean lazy = false;
    @Nullable final private String webhookUrl;
    @NotNull final private String bucketName;
    @Nullable final private TransformationFilter[] filters;
}
