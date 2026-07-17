package com.example.uploads_api.uploads;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record UploadId(
        @NotNull UUID get
) {
}
