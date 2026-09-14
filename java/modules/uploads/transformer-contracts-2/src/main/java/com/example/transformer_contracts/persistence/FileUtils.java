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

    /**
     * Return the absolute paths of the files in a directory recursively.
     */
    public List<Path> getFilesInDir(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .toList();
        }
    }

    /** Delete all entries in a directory and the directory itself. */
    public void deleteRecursively(@NonNull Path path) throws IOException {
        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(entry -> {
                try {
                    Files.deleteIfExists(entry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            // TODO do I need to delete the root directory too?
        }
    }
}
