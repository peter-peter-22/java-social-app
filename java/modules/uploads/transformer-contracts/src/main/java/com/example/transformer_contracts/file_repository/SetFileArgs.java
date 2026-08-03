package com.example.transformer_contracts.file_repository;

import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public record SetFileArgs(
        @NonNull String key,
        @NonNull Path path,
        @NonNull String contentType
) {
}
