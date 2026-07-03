package com.example.media.uploads.upload_repository;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record SignedUploadDTO(
        @NotNull Map<String, String> postHeaders,
        @NotNull String bucketName,
        @NotNull String objectPath
) {
}
