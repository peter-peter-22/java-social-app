package com.example.transformer_contracts.persistence;

import com.example.transformer_contracts.file_repository.ProcessedFileRepository;
import com.example.uploads_api.transformations.asset.Asset;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TransformationPersistenceSession implements AutoCloseable {
    @NonNull
    private final Asset asset;
    @NonNull
    private final ProcessedFileRepository processedFileRepository;

    @Getter
    @NonNull
    private Path inputFile;
    @NonNull
    private final static Path tempDir;
    @NonNull
    private Path groupDir;

    static {
        try {
            tempDir = Files.createTempDirectory("upload_transformer");
        } catch (IOException e) {
            throw new RuntimeException("Failed create temp folder", e);
        }
    }

    public TransformationPersistenceSession(
            @NonNull Asset asset,
            @NonNull ProcessedFileRepository processedFileRepository
    ) {
        this.asset = asset;
        this.processedFileRepository = processedFileRepository;

        initialize();
    }

    private void initialize() {
        try {
            groupDir = tempDir.resolve(asset.assetId());
            Files.createDirectories(groupDir);

            inputFile = groupDir.resolve("input." + asset.format().getExtension());
            processedFileRepository.downloadOriginal(inputFile, asset);
        } catch (IOException e) {
            throw new RuntimeException("IO error during initialization", e);
        }
    }

    public TaskSession createTaskSession(String transformationName) {
        var taskDir = groupDir.resolve(transformationName);
        return new TaskSession(
                taskDir,
                processedFileRepository::uploadTransformations
        );
    }

    @Override
    public void close() {
        try {
            FileUtils.deleteRecursively(groupDir);
        } catch (IOException e) {
            throw new RuntimeException("IO error during close", e);
        }
    }
}
