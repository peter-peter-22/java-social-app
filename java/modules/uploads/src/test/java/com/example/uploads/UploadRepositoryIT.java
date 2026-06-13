package com.example.uploads;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.uploads.upload_repository.*;
import com.example.users.repository.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class UploadRepositoryIT extends CockroachIntegrationTest {

    @Autowired
    private UploadRepository uploadRepository;
    // TODO: check the user id foreign key constraint when the user repository is implemented

    @Test
    void testUploadCRUD() {
        InsertUpload upload = new InsertUpload(
                new UserId(UUID.randomUUID()),
                "bucket",
                MediaType.IMAGE,
                "group",
                0,
                UploadStatus.UPLOADING,
                "jpg"
        );

        // The created upload should return its id
        var id = uploadRepository.create(upload);
        assertThat(id).isNotNull();

        // The created upload should be found and have the same fields
        var found = uploadRepository.getById(id);
        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(id);
        assertThat(found.status()).isEqualTo(upload.status());
        assertThat(found.mediaType()).isEqualTo(upload.mediaType());
        assertThat(found.transformationGroup()).isEqualTo(upload.transformationGroup());
        assertThat(found.transformationVersion()).isEqualTo(upload.transformationVersion());
        assertThat(found.fileExtension()).isEqualTo(upload.fileExtension());

        // Check generated fields
        assertThat(found.createdAt()).isNotNull();

        // All fields should be updated
        var update = new Upload(
                id,
                new UserId(UUID.randomUUID()),
                "bucket 2",
                MediaType.VIDEO,
                found.createdAt().plus(1, ChronoUnit.DAYS),
                "group 2",
                1,
                UploadStatus.READY,
                "extension 2"
        );
        uploadRepository.update(update);
        var updated = uploadRepository.getById(id);
        assertThat(updated).isEqualTo(update);
    }

    @Test
    void testUserForeignKey() {
        System.out.println("not implemented yet");
    }
}
