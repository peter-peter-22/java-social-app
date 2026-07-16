package com.example.image_transformer.storage;

import com.example.media_api.uploads.ObjectLocation;
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
public class LocalStreamStorage implements FileStreamStorage {
    public static Path objectLocationToLocalPath(ObjectLocation location) {
        return TestResourcesDirectory.getResourcesPath().resolve(location.bucket()).resolve(location.path());
    }

    @Override
    public void write(
            @NotNull InputStream inputStream,
            @NotNull ObjectLocation outputLocation
    ) {
        var outputPath = objectLocationToLocalPath(outputLocation);

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
    public @NotNull InputStream read(@NotNull ObjectLocation inputLocation) {
        var inputPath = objectLocationToLocalPath(inputLocation);
        try {
            return Files.newInputStream(inputPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read source image from " + inputPath, e);
        }
    }
}
