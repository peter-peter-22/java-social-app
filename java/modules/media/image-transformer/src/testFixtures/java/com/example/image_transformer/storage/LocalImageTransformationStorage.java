package com.example.image_transformer.storage;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Profile("local")
class LocalImageTransformationStorage implements TransformationImageStorage {
    private static final String DEFAULT_TEST_IMAGE = "image.jpg";

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

    @Override
    public @NotNull VImage read(@NotNull Arena arena, @NotNull UploadTransformationDTO upload) {
        return VImage.newFromFile(
                arena,
                testResourcesDirectory().resolve(DEFAULT_TEST_IMAGE).toString(),
                VipsOption.Boolean("autorotate", true)
        );
    }

    // TODO too long?
    private Path testResourcesDirectory() {
        var integrationRelative = Path.of("src/integrationTest/resources");
        if (Files.isDirectory(integrationRelative)) {
            return integrationRelative;
        }

        var testRelative = Path.of("src/test/resources");
        if (Files.isDirectory(testRelative)) {
            return testRelative;
        }

        var moduleIntegrationRelative = Path.of("modules/media/image-transformer/src/integrationTest/resources");
        if (Files.isDirectory(moduleIntegrationRelative)) {
            return moduleIntegrationRelative;
        }

        return Path.of("modules/media/image-transformer/src/test/resources");
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
