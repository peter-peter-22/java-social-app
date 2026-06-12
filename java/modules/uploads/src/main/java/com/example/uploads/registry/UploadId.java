package com.example.uploads.registry;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UploadId(@NotNull UUID get) {
}
