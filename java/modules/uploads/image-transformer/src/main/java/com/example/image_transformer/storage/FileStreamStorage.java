package com.example.image_transformer.storage;

import com.example.uploads_api.uploads.ObjectLocation;
import org.jspecify.annotations.NonNull;

import java.io.InputStream;

public interface FileStreamStorage {
    void write(
            @NonNull InputStream inputStream,
            @NonNull ObjectLocation outputLocation
    );

    @NonNull InputStream read(@NonNull ObjectLocation inputLocation);
}
