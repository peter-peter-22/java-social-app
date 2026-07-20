package com.example.image_transformer.storage;

import com.example.uploads_api.uploads.ObjectLocation;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
@Profile("local")
public class LocalStreamStorage implements FileStreamStorage {
    public static @NonNull Path objectLocationToLocalPath(@NonNull ObjectLocation location) {
        return TestResourcesDirectory.getResourcesPath().resolve(location.bucket()).resolve(location.key());
    }

    @Override
    public void write(
            @NonNull InputStream inputStream,
            @NonNull ObjectLocation outputLocation
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
    public @NonNull InputStream read(@NonNull ObjectLocation inputLocation) {
        var inputPath = objectLocationToLocalPath(inputLocation);
        try {
            return Files.newInputStream(inputPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read source image from " + inputPath, e);
        }
    }
}
