package com.example.object_storage.repository;

import com.example.uploads_api.uploads.ObjectLocation;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.TimeUnit;

@Builder
@Getter
public class GetPreSignedDownloadUrlArgs {
    private final @NonNull ObjectLocation location;
    private final @NonNull Integer expiresIn;
    private final @NonNull TimeUnit timeUnit = TimeUnit.SECONDS;
}
