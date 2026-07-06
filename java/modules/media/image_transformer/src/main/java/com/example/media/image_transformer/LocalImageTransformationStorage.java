package com.example.media.image_transformer;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
class LocalImageTransformationStorage implements TransformationImageStorage {
    private static final String DEFAULT_TEST_IMAGE = "image.jpg";

    @Override
    @NotNull
    public Path input(@NotNull UploadTransformationDTO upload) {
        var resources = testResourcesDirectory();
        var objectFileName = Path.of(upload.original().objectPath()).getFileName();
        var requestedFile = objectFileName == null ? null : resources.resolve(objectFileName);

        return requestedFile != null && Files.exists(requestedFile)
                ? requestedFile
                : resources.resolve(DEFAULT_TEST_IMAGE);
    }

    @Override
    public void write(
            @NotNull VImage image,
            @NotNull UploadTransformationDTO upload,
            @NotNull ImageTransformationOperations operations
    ) {
        image.jpegsave(
                testResourcesDirectory().resolve(
                        "output_" + sanitize(upload.outputBucket()) + "_" + sanitize(upload.name()) + ".jpg"
                ).toString(),
                VipsOption.Int("Q", operations.getQuality() == null ? 100 : operations.getQuality())
        );
    }

    private Path testResourcesDirectory() {
        var moduleRelative = Path.of("src/test/resources");
        return Files.isDirectory(moduleRelative)
                ? moduleRelative
                : Path.of("modules/media/image_transformer/src/test/resources");
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
