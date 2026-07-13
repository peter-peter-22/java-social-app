package com.example.image_transformer.storage;

import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.object_storage.repository.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
@Profile("!local")
@RequiredArgsConstructor
public class ObjectStorageTransformationStorage implements TransformationImageStorage {
    private final ObjectStorageRepository objectStorageRepository;

    @Override
    public void write(@NotNull InputStream inputStream, @NotNull UploadTransformationTask upload) {
        var outputId = upload.getOutputObject();

        try {
            var bytes = inputStream.readAllBytes();
            objectStorageRepository.putObject(
                    outputId.bucket(),
                    outputId.objectPath(),
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    "image/jpeg"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload transformed image to object storage", e);
        }
    }

    @Override
    public @NotNull InputStream read(@NotNull UploadTransformationTask upload) {
        try {
            return objectStorageRepository.getObject(
                    upload.getOriginal().bucket(),
                    upload.getOriginal().objectPath()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read source image from object storage", e);
        }
    }
}
