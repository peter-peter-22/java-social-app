package com.example.uploads;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.uploads.upload_repository.*;
import com.example.uploads.upload_variant_repository.*;
import com.example.users.repository.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@SpringBootTest
@Testcontainers
public class UploadVariantRepositoryIT extends CockroachIntegrationTest {

    @Autowired
    private UploadRepository uploadRepo;
    @Autowired
    private UploadVariantRepository uploadVariantRepo;

    // TODO: check the user id foreign key constraint when the user repository is implemented

    @Test
    void testUploadVariantCRUD() {
        InsertUpload upload = new InsertUpload(
                new UserId(UUID.randomUUID()),
                "bucket",
                MediaType.IMAGE,
                "group",
                0,
                UploadStatus.UPLOADING,
                "jpg"
        );

        // The created upload variant should return its id
        var uploadId = uploadRepo.create(upload);

        var insert = new InsertUploadVariant(
                new UploadVariantId(uploadId, "variant"),
                "bucket",
                "jpg",
                UploadVariantStatus.READY
        );

        var variantId = uploadVariantRepo.create(insert);
        assertThat(variantId).isNotNull();

        // The created upload should be found and have the same fields
        var found = uploadVariantRepo.getById(variantId);
        assertThat(found).isNotNull();
        assertThat(found.key().originId()).isEqualTo(uploadId);
        assertThat(found.bucketName()).isEqualTo(insert.bucketName());
        assertThat(found.status()).isEqualTo(insert.status());
        assertThat(found.fileExtension()).isEqualTo(upload.fileExtension());

        // Check generated fields
        assertThat(found.createdAt()).isNotNull();

        // All fields should be updated
        var update = new UploadVariant(
                found.key(),
                "bucket 2",
                "extension 2",
                found.createdAt().plus(1, ChronoUnit.DAYS),
                UploadVariantStatus.CREATING
        );
        uploadVariantRepo.update(update);
        var updated = uploadVariantRepo.getById(variantId);
        assertThat(updated).isEqualTo(update);
    }

    @Test
    void testUploadForeignKey() {
        // The upload variant should not be created if the upload does not exist
        var insert = new InsertUploadVariant(
                new UploadVariantId(new UploadId(UUID.randomUUID()), "variant"),
                "bucket",
                "jpg",
                UploadVariantStatus.READY
        );
        try {
            uploadVariantRepo.create(insert);
        } catch (DataIntegrityViolationException e) {
            return;
        }
        fail("The foreign key constraint should have been violated.");
    }
}
