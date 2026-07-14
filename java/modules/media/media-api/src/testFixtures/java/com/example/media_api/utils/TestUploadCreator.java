package com.example.media_api.utils;

import com.example.media_api.uploads.*;
import com.example.users.api.repository.UserId;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

public class TestUploadCreator {
    public static @NotNull Upload createUploadFromPath(@NotNull String path) {
        return createUploadFromLocation(new ObjectLocation(path, "bucket"+UUID.randomUUID()));
    }

    public static @NotNull Upload createUploadFromBucket(@NotNull String bucket) {
        return createUploadFromLocation(new ObjectLocation("example.file"+UUID.randomUUID(), bucket));
    }

    public static @NotNull Upload createUploadFromLocation(@NotNull ObjectLocation location) {
        return new Upload(
                new UploadId(UUID.randomUUID()),
                new ObjectLocation(location.path(), location.bucket()),
                new UserId(UUID.randomUUID()),
                FileType.JPEG,
                Instant.now(),
                UploadStatus.READY
        );
    }

    public static @NotNull Upload createUploadFromFileType(@NotNull FileType fileType) {
        return new Upload(
                new UploadId(UUID.randomUUID()),
                new ObjectLocation("example.file"+UUID.randomUUID(), "bucket"+UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                fileType,
                Instant.now(),
                UploadStatus.READY
        );
    }

    public static @NotNull Upload createUpload(){
        return createUploadFromFileType(FileType.JPEG);
    }
}
