package com.example.transformer_contracts.persistence;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@UtilityClass
public class FileUtils {

    public List<Path> getFilesInDir(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .toList();
        }
    }

    public void deleteRecursively(@NonNull Path path) throws IOException {
        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(entry -> {
                try {
                    Files.deleteIfExists(entry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
