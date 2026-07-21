package com.example.uploads_persistence.utils;

import com.example.uploads_api.transformations.upload_repository.InsertUpload;
import com.example.uploads_api.uploads.FileType;
import com.example.uploads_api.uploads.UploadId;
import com.example.uploads_persistence.upload_repository.UploadRepositoryImpl;
import com.example.users_api.repository.InsertUser;
import com.example.users_persistence.utils.TestUserPersistence;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class TestUploadPersistence {
    private final TestUserPersistence userPersistence;
    private final UploadRepositoryImpl uploadRepository;

    public InsertUpload prepareUploadInsert(
            @Nullable Consumer<InsertUpload.@NonNull InsertUploadBuilder> uploadCustomizer,
            @Nullable Consumer<InsertUser.@NonNull InsertUserBuilder> userCustomizer
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

    public InsertUpload prepareUploadInsert() {
        return prepareUploadInsert(null, null);
    }

    public UploadId insertUpload(
            @Nullable Consumer<InsertUpload.@NonNull InsertUploadBuilder> uploadCustomizer,
            @Nullable Consumer<InsertUser.@NonNull InsertUserBuilder> userCustomizer
    ) {
        var insert = prepareUploadInsert(uploadCustomizer, userCustomizer);
        return uploadRepository.create(insert);
    }

    public UploadId insertUpload(
            @Nullable Consumer<InsertUpload.@NonNull InsertUploadBuilder> uploadCustomizer
    ) {
        return insertUpload(uploadCustomizer, null);
    }

    public UploadId insertUpload() {
        return insertUpload(null, null);
    }
}
