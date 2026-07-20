package com.example.uploads_service.upload_service;

import com.example.uploads_api.uploads.FileType;
import com.example.users_api.repository.UserId;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.time.temporal.TemporalUnit;

@Builder
@Getter
public class CreateSignedUploadArgs {
    private final @NonNull String objectPath;
    private final @NonNull FileType fileType;
    private final @NonNull UserId createdBy;
    private final @NonNull Integer expiration;
    private final @NonNull TemporalUnit timeUnit;
    private final @NonNull Integer maxContentLengthBytes;
    private final @NonNull Integer minLengthBytes;
    private final @NonNull Integer maxLengthBytes;
}