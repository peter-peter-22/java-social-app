package com.example.transformer_contracts.persistence;

import com.example.transformer_contracts.file_repository.SetFileArgs;
import com.example.uploads_api.uploads.FileType;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

public class TaskSession {
    @Getter
    private final Path taskDir;
    private final Consumer<Collection<SetFileArgs>> submitTransformations;

    public TaskSession(
            Path taskDir,
            Consumer<Collection<SetFileArgs>> submitTransformations
    ) {
        this.submitTransformations = submitTransformations;
        this.taskDir = taskDir;

        initialize();
    }

    private void initialize() {
        try {
            Files.createDirectories(taskDir);
        } catch (IOException e) {
            throw new RuntimeException("IO error during creating task dir", e);
        }
    }

    public void submitTaskResults(@NonNull String name) {
        try {
            var outputPaths = PersistenceUtils.getFilesInDir(taskDir);
            var uploads = outputPaths.stream()
                    .map(path -> new SetFileArgs(
                            taskDir.relativize(path).toString(),
                            path,
                            FileType.getContentTypeFromPath(path)
                    ))
                    .toList();
            submitTransformations.accept(uploads);
            PersistenceUtils.deleteRecursively(taskDir);
        } catch (IOException e) {
            throw new RuntimeException("IO error while submitting task", e);
        }
    }
}
