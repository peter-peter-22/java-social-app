package com.example.image_transformer.storage;

import com.example.media_api.transformations.task.UploadTransformationTask;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

@Component
@Profile("local")
class LocalImageTransformationStorage implements TransformationImageStorage {
    @Override
    public void write(
            @NotNull InputStream inputStream,
            @NotNull UploadTransformationTask upload
    ) {
        var outputId = upload.getOutputId();
        var outputPath = testResourcesDirectory().resolve(outputId.objectPath());

        try {
            var parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create output directories for " + outputPath, e);
        }

        try {
            Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write transformed image to " + outputPath, e);
        }
    }

    @Override
    public @NotNull InputStream read(@NotNull UploadTransformationTask upload) {
        var inputPath = testResourcesDirectory().resolve(upload.getOriginal().objectPath());
        try {
            return Files.newInputStream(inputPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read source image from " + inputPath, e);
        }
    }

    // TODO overcomplicated?
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
}
