package com.example.image_transformer.storage;

import com.example.media_api.uploads.ObjectLocation;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public interface FileStreamStorage {
    void write(
            @NotNull InputStream inputStream,
            @NotNull ObjectLocation outputLocation
    );

    @NotNull InputStream read(@NotNull ObjectLocation inputLocation);
}
