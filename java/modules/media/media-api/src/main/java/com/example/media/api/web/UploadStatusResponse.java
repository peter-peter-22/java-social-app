package com.example.media.api.web;

import com.example.media.contract.upload.UploadAttemptStatus;

/** The status of the key's active upload attempt. */
public record UploadStatusResponse(String uploadKey, UploadAttemptStatus status) {
}
