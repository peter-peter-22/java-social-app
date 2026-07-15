package com.example.uploads.utils;

import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.UploadId;
import com.example.uploads.upload_repository.InsertUpload;
import com.example.uploads.upload_repository.UploadRepository;
import com.example.users_persistence.utils.TestUserPersistence;
import com.example.users_persistence.repository.InsertUser;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class TestUploadPersistence {
    private final TestUserPersistence userPersistence;
    private final UploadRepository uploadRepository;

    public InsertUpload prepareUploadInsert(
            @Nullable Consumer<InsertUpload.@NotNull InsertUploadBuilder> uploadCustomizer,
            @Nullable Consumer<InsertUser.@NotNull InsertUserBuilder> userCustomizer
    ) {
        var user = userPersistence.insertUser(userCustomizer);

        var builder = InsertUpload.builder()
                .objectPath(UUID.randomUUID() + ".jpg")
                .bucket("bucket")
                .fileType(FileType.JPEG)
                .createdBy(user.id());

        if (uploadCustomizer != null)
            uploadCustomizer.accept(builder);

        return builder.build();
    }

    public InsertUpload prepareUploadInsert(
            @Nullable Consumer<InsertUpload.@NotNull InsertUploadBuilder> uploadCustomizer
    ) {
        return prepareUploadInsert(uploadCustomizer, null);
    }

    public InsertUpload prepareUploadInsert() {
        return prepareUploadInsert(null, null);
    }

    public UploadId insertUpload(
            @Nullable Consumer<InsertUpload.@NotNull InsertUploadBuilder> uploadCustomizer,
            @Nullable Consumer<InsertUser.@NotNull InsertUserBuilder> userCustomizer
    ) {
        var insert = prepareUploadInsert(uploadCustomizer, userCustomizer);
        return uploadRepository.create(insert);
    }

    public UploadId insertUpload(
            @Nullable Consumer<InsertUpload.@NotNull InsertUploadBuilder> uploadCustomizer
    ) {
        return insertUpload(uploadCustomizer, null);
    }

    public UploadId insertUpload() {
        return insertUpload(null, null);
    }
}
