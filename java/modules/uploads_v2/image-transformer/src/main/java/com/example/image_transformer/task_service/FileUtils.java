package com.example.image_transformer.task_service;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@UtilityClass
@NullMarked
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

    /**
     * Delete all entries in a directory and the directory itself.
     */
    public void deleteRecursively(Path path) throws IOException {
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

    /**
     * Return the file extension of a string. Return null if no extension found.
     */
    @Nullable String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return null;
    }

    /**
     * Get the sum of the size of the given files.
     */
    long getSizeSum(Collection<Path> paths) {
        return paths.stream()
                .map(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to get the size of a file", e);
                    }
                })
                .reduce(0L, Long::sum);
    }
}
