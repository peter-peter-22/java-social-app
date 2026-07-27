package com.example.transformer_contracts.webhook;

import com.example.uploads_api.uploads.UploadId;
import org.jspecify.annotations.NonNull;

public interface HasWebhookCall {
    boolean lazy();

    @NonNull UploadId uploadId();

    @NonNull String name();
}
