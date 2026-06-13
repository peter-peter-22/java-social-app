package com.example.uploads.upload_repository;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UploadId(@NotNull UUID get) {
}
