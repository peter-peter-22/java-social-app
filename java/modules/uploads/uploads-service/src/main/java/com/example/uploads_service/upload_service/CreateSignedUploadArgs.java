package com.example.uploads_service.upload_service;

import com.example.uploads_api.uploads.FileType;
import com.example.users_api.repository.UserId;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.TemporalUnit;

@Builder
@Getter
public class CreateSignedUploadArgs {
    private final @NotNull String objectPath;
    private final @NotNull FileType fileType;
    private final @NotNull UserId createdBy;
    private final @NotNull Integer expiration;
    private final @NotNull TemporalUnit timeUnit;
    private final @NotNull Integer maxContentLengthBytes;
    private final @NotNull Integer minLengthBytes;
    private final @NotNull Integer maxLengthBytes;
}