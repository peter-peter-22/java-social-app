package com.example.uploads;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record UploadId(@NotNull UUID get) { // TODO: add bucket
}
