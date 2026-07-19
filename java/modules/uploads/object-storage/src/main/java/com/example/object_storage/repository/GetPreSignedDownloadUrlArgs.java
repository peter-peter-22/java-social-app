package com.example.object_storage.repository;

import com.example.uploads_api.uploads.ObjectLocation;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.TimeUnit;

@Builder
@Getter
public class GetPreSignedDownloadUrlArgs {
    private final @NotNull @NonNull ObjectLocation location;
    private final @NotNull Integer expiresIn;
    private final @NotNull TimeUnit timeUnit = TimeUnit.SECONDS;
}
