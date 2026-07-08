package com.example.object_storage.repository;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@Builder
@Getter
public class GetPreSignedDownloadUrlArgs {
    private final @NotNull String bucket;
    private final @NotNull String objectPath;
    private final @NotNull Integer expiresIn;
    private final @NotNull TimeUnit timeUnit = TimeUnit.SECONDS;
}
