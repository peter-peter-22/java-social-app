package com.example.uploads_service.upload_service;

import com.example.uploads_api.uploads.UploadId;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@Builder
public record SignedUploadFormResponse(
        @NonNull Map<String, String> formFields,
        @NonNull UploadId uploadId,
        @NonNull String uploadUrl
) {
}
