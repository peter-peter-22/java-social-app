package com.example.image_transformer.storage;

import com.example.object_storage.repository.ObjectStorageRepository;
import com.example.uploads_api.uploads.ObjectLocation;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
@Profile("!local")
@RequiredArgsConstructor
public class ObjectStorageStream implements FileStreamStorage {
    private final ObjectStorageRepository objectStorageRepository;

    @Override
    public void write(
            @NonNull InputStream inputStream,
            @NonNull ObjectLocation outputLocation
    ) {
        try {
            var bytes = inputStream.readAllBytes();
            objectStorageRepository.putObject(
                    outputLocation,
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    "image/jpeg"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload transformed image to object storage", e);
        }
    }

    @Override
    public @NonNull InputStream read(@NonNull ObjectLocation inputLocation) {
        try {
            return objectStorageRepository.getObject(inputLocation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read source image from object storage", e);
        }
    }
}
