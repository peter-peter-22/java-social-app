package com.example.transformer_contracts.file_repository;

import com.example.object_storage.repository.ObjectStorageRepository;
import com.example.uploads_api.uploads.ObjectLocation;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
abstract class FileBucketRepository {
    private final ObjectStorageRepository objectStorageRepository;

    protected abstract @NonNull String bucketName();

    public void get(@NonNull String key, @NonNull Path path) {
        objectStorageRepository.downloadObject(new ObjectLocation(key, bucketName()), path);
    }

    public void set(@NonNull SetFileArgs args) {
        objectStorageRepository.uploadObject(new ObjectLocation(args.key(), bucketName()), args.path(), args.contentType());
    }

    public void setAll(@NonNull Collection<@NonNull SetFileArgs> entries) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            var futures = entries.stream()
                    .map(entry -> CompletableFuture.runAsync(() -> set(entry), executor))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
        }
    }
}
