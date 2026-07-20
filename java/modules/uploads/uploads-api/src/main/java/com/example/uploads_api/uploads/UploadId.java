package com.example.uploads_api.uploads;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record UploadId(
        @NonNull UUID get
) {
}
