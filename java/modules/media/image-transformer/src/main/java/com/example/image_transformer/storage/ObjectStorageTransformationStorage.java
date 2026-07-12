package com.example.image_transformer.storage;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.object_storage.repository.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.lang.foreign.Arena;

@Component
@Profile("!local")
@RequiredArgsConstructor
public class ObjectStorageTransformationStorage implements TransformationImageStorage {
    private final ObjectStorageRepository objectStorageRepository;

    @Override
    public void write(@NotNull VImage image, @NotNull UploadTransformationTask upload, @NotNull ImageTransformationOperations operations) {
        var jpegData = image.jpegsaveBuffer(
                VipsOption.Int("Q", operations.getQuality() == null ? 100 : operations.getQuality())
        );

        var outputId = upload.getOutputId();

        try (var inputStream = new ByteArrayInputStream(jpegData.getBytes())) {
            objectStorageRepository.putObject(
                    outputId.bucket(),
                    outputId.objectPath(),
                    inputStream,
                    jpegData.byteSize(),
                    "image/jpeg"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload transformed image to object storage", e);
        }
    }

    @Override
    public @NotNull VImage read(@NotNull Arena arena, @NotNull UploadTransformationTask upload) {
        try (var inputStream = objectStorageRepository.getObject(
                upload.getOriginal().bucket(),
                upload.getOriginal().objectPath()
        )) {
            return VImage.newFromStream(
                    arena,
                    inputStream,
                    VipsOption.Boolean("autorotate", true)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read source image from object storage", e);
        }
    }
}
