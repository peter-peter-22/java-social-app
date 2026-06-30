package com.example.uploads;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.api.MediaType;
import com.example.api.Upload;
import com.example.api.UploadId;
import com.example.uploads.upload_repository.*;
import com.example.users.repository.InsertUser;
import com.example.users.repository.UserId;
import com.example.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@Testcontainers
public class UploadRepositoryIT extends CockroachIntegrationTest {

    @Autowired
    private UploadRepository uploadRepository;
    @Autowired
    private UserRepository userRepository;

    private UploadId initUpload() {
        InsertUser insertUser = new InsertUser();
        var user = userRepository.create(insertUser);

        InsertUpload upload = new InsertUpload(
                user.id(),
                "bucket",
                MediaType.IMAGE,
                com.example.api.UploadStatus.UPLOADING,
                "jpg"
        );
        return uploadRepository.create(upload);
    }

    /**
     * The created upload should return its id
     */
    @Test
    void createUpload() {
        var id = initUpload();
        assertThat(id).isNotNull();
    }

    /**
     * The created upload should be found and have the same fields as the inserted one
     */
    @Test
    void testUploadCRUD() {

        // Insert user
        InsertUser insertUser = new InsertUser();
        var user = userRepository.create(insertUser);

        // Insert upload
        InsertUpload upload = new InsertUpload(
                user.id(),
                "bucket",
                MediaType.IMAGE,
                com.example.api.UploadStatus.UPLOADING,
                "jpg"
        );
        var id = uploadRepository.create(upload);

        // The created upload should be found and have the same fields
        var found = uploadRepository.getById(id);
        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(id);
        assertThat(found.status()).isEqualTo(upload.status());
        assertThat(found.mediaType()).isEqualTo(upload.mediaType());
        assertThat(found.fileExtension()).isEqualTo(upload.fileExtension());

        // Check generated fields
        assertThat(found.createdAt()).isNotNull();
    }

    /**
     * All fields should be updated
     */
    @Test
    void testUploadUpdate() {
        var id = initUpload();
        var original = uploadRepository.getById(id);
        assertThat(original).isNotNull();

        // All fields should be updated
        var otherUser = userRepository.create(new InsertUser());
        var update = new Upload(
                id,
                otherUser.id(),
                "bucket 2",
                MediaType.VIDEO,
                original.createdAt().plus(1, ChronoUnit.DAYS),
                com.example.api.UploadStatus.READY,
                "extension 2"
        );
        uploadRepository.update(update);
        var updated = uploadRepository.getById(id);
        assertThat(updated).isEqualTo(update);
    }

    /**
     * The upload should create a foreign key error if the user does not exist
     */
    @Test
    void testUserForeignKey() {
        InsertUpload upload = new InsertUpload(
                new UserId(UUID.randomUUID()),
                "bucket",
                MediaType.IMAGE,
                com.example.api.UploadStatus.UPLOADING,
                "jpg"
        );
        try {
            uploadRepository.create(upload);
        } catch (UploadMissingUserException e) {
            return;
        }
        fail("The foreign key constraint should have been violated.");
    }
}
