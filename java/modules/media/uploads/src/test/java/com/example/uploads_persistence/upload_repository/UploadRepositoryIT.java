package com.example.uploads_persistence.upload_repository;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.ObjectLocation;
import com.example.media_api.uploads.UploadStatus;
import com.example.media_api.utils.TestUploadCreator;
import com.example.uploads_persistence.TestApplication;
import com.example.uploads_persistence.utils.TestUploadPersistence;
import com.example.users_api.repository.UserId;
import com.example.users_persistence.utils.TestUserPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(locations = "classpath:uploads-test.properties")
public class UploadRepositoryIT extends CockroachIntegrationTest {
    @Autowired
    private TestUploadPersistence testUploadPersistence;

    @Autowired
    private TestUserPersistence testUserPersistence;

    @Autowired
    private UploadRepository uploadRepository;

    /**
     * The created upload should return its id
     */
    @Test
    void createUpload() {
        var id = testUploadPersistence.insertUpload();
        assertThat(id).isNotNull();
    }

    /**
     * The created upload should be found and have the same fields as the inserted one
     */
    @Test
    void testUploadRead() {

        // Insert upload
        var upload = testUploadPersistence.prepareUploadInsert();
        var id = uploadRepository.create(upload);

        // The created upload should be found and have the same fields
        var found = uploadRepository.getById(id);
        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(id);
        assertThat(found.status()).isEqualTo(upload.getStatus());
        assertThat(found.fileType()).isEqualTo(upload.getFileType());

        // Check generated fields
        assertThat(found.createdAt()).isNotNull();
    }

    /**
     * All fields should be updated
     */
    @Test
    void testUploadUpdate() {
        var id = testUploadPersistence.insertUpload();
        var original = uploadRepository.getById(id);
        assertThat(original).isNotNull();

        // All fields should be updated
        var otherUser = testUserPersistence.insertUser();
        var update = TestUploadCreator.createUpload(
                c->{
                    c.id(id);
                    c.objectLocation(new ObjectLocation("path 2", "bucket 2"));
                    c.createdBy(otherUser.id());
                    c.fileType(FileType.JPEG);
                    c.createdAt(original.createdAt().plus(1, ChronoUnit.DAYS));
                    c.status(UploadStatus.READY);
                }
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
        var upload = InsertUpload.builder()
                .objectPath(UUID.randomUUID() + ".jpg")
                .bucket("bucket")
                .fileType(FileType.JPEG)
                .createdBy(new UserId(UUID.randomUUID()))
                .build();
        try {
            uploadRepository.create(upload);
        } catch (UploadMissingUserException e) {
            return;
        }
        fail("The foreign key constraint should have been violated.");
    }

    // TODO: test status update and existence check
}
