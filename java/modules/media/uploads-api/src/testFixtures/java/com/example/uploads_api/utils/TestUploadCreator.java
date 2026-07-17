package com.example.uploads_api.utils;

import com.example.uploads_api.uploads.*;
import com.example.users_api.repository.UserId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

public class TestUploadCreator {
    public static @NotNull Upload createUploadFromPath(@NotNull String path) {
        return createUpload(
                c -> c.objectLocation(new ObjectLocation(path, "bucket" + UUID.randomUUID()))
        );
    }

    public static @NotNull Upload createUploadFromBucket(@NotNull String bucket) {
        return createUpload(
                c -> c.objectLocation(new ObjectLocation("example.file" + UUID.randomUUID(), bucket))
        );
    }

    public static @NotNull Upload createUpload(@Nullable Consumer<Upload.@NotNull UploadBuilder> customizer) {
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

    public static @NotNull Upload createImage(){
        return createUpload(c->c.fileType(FileType.JPEG));
    }

    public static @NotNull Upload createVideo(){
        return createUpload(c->c.fileType(FileType.MP4));
    }
}
