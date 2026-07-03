package com.example.media.uploads;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.media.common.uploads.*;
import com.example.users.repository.InsertUser;
import com.example.users.repository.UserId;
import com.example.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.media.uploads.upload_repository.InsertUpload;
import com.example.media.uploads.upload_repository.UploadMissingUserException;
import com.example.media.uploads.upload_repository.UploadRepository;

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

    private InsertUpload prepareUpload() {
        InsertUser insertUser = new InsertUser();
        var user = userRepository.create(insertUser);

        return new InsertUpload(
                "bucket",
                UUID.randomUUID() + ".jpg",
                FileType.JPEG,
                UploadStatus.UPLOADING,
                user.id()
        );
    }

    private UploadId insertUpload() {
        var insert = prepareUpload();
        return uploadRepository.create(insert);
    }

    /**
     * The created upload should return its id
     */
    @Test
    void createUpload() {
        var id = insertUpload();
        assertThat(id).isNotNull();
    }

    /**
     * The created upload should be found and have the same fields as the inserted one
     */
    @Test
    void testUploadRead() {

        // Insert upload
        var upload = prepareUpload();
        var id = uploadRepository.create(upload);

        // The created upload should be found and have the same fields
        var found = uploadRepository.getById(id);
        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(id);
        assertThat(found.status()).isEqualTo(upload.status());
        assertThat(found.fileType()).isEqualTo(upload.fileType());

        // Check generated fields
        assertThat(found.createdAt()).isNotNull();
    }

    /**
     * All fields should be updated
     */
    @Test
    void testUploadUpdate() {
        var id = insertUpload();
        var original = uploadRepository.getById(id);
        assertThat(original).isNotNull();

        // All fields should be updated
        var otherUser = userRepository.create(new InsertUser());
        var update = new Upload(
                id,
                otherUser.id(),
                FileType.JPEG,
                original.createdAt().plus(1, ChronoUnit.DAYS),
                UploadStatus.READY
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
                "bucket",
                UUID.randomUUID() + ".jpg",
                FileType.JPEG,
                UploadStatus.UPLOADING,
                new UserId(UUID.randomUUID())
        );
        try {
            uploadRepository.create(upload);
        } catch (UploadMissingUserException e) {
            return;
        }
        fail("The foreign key constraint should have been violated.");
    }

    // TODO: test status update and existence check
}
