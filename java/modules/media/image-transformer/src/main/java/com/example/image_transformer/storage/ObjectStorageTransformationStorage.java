package com.example.image_transformer.storage;

import app.photofox.vipsffm.VImage;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.object_storage.repository.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.foreign.Arena;

@Component
@RequiredArgsConstructor
public class ObjectStorageTransformationStorage implements TransformationImageStorage {
    private final ObjectStorageRepository objectStorageRepository;

    @Override
    public void write(@NotNull VImage image, @NotNull UploadTransformationDTO upload, @NotNull ImageTransformationOperations operations) {

    }

    @Override
    public @NotNull VImage read(@NotNull Arena arena, @NotNull UploadTransformationDTO upload) {
        return null;
    }
}
