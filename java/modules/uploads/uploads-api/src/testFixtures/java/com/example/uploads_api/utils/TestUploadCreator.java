package com.example.uploads_api.utils;

import com.example.uploads_api.uploads.*;
import com.example.users_api.repository.UserId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

public class TestUploadCreator {
    public static @NonNull Upload createUploadFromPath(@NonNull String path) {
        return createUpload(
                c -> c.objectLocation(new ObjectLocation(path, "bucket" + UUID.randomUUID()))
        );
    }

    public static @NonNull Upload createUploadFromBucket(@NonNull String bucket) {
        return createUpload(
                c -> c.objectLocation(new ObjectLocation("example.file" + UUID.randomUUID(), bucket))
        );
    }

    public static @NonNull Upload createUpload(@Nullable Consumer<Upload.@NonNull UploadBuilder> customizer) {
        var builder = Upload.builder()
                .id(new UploadId(UUID.randomUUID()))
                .objectLocation(new ObjectLocation("example.file" + UUID.randomUUID(), "bucket" + UUID.randomUUID()))
                .createdBy(new UserId(UUID.randomUUID()))
                .fileType(FileType.JPEG)
                .createdAt(Instant.now())
                .status(UploadStatus.READY);

        if (customizer != null)
            customizer.accept(builder);

        return builder.build();
    }

    public static @NonNull Upload createImage() {
        return createUpload(c->c.fileType(FileType.JPEG));
    }

    public static @NonNull Upload createVideo() {
        return createUpload(c->c.fileType(FileType.MP4));
    }
}
