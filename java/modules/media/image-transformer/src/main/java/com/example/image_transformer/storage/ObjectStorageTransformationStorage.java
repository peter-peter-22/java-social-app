package com.example.image_transformer.storage;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.object_storage.repository.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Primary;
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
    public void write(@NotNull VImage image, @NotNull UploadTransformationDTO upload, @NotNull ImageTransformationOperations operations) {
        var jpegData = image.jpegsaveBuffer(
                VipsOption.Int("Q", operations.getQuality() == null ? 100 : operations.getQuality())
        );

        try (var inputStream = new ByteArrayInputStream(jpegData.getBytes())) {
            objectStorageRepository.putObject(
                    upload.outputBucket(),
                    outputObjectPath(upload),
                    inputStream,
                    jpegData.byteSize(),
                    "image/jpeg"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload transformed image to object storage", e);
        }
    }

    @Override
    public @NotNull VImage read(@NotNull Arena arena, @NotNull UploadTransformationDTO upload) {
        try (var inputStream = objectStorageRepository.getObject(
                upload.original().bucket(),
                upload.original().objectPath()
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

    private String outputObjectPath(@NotNull UploadTransformationDTO upload) {
        return "output_" + sanitize(upload.outputBucket()) + "_" + sanitize(upload.name()) + ".jpg";
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
