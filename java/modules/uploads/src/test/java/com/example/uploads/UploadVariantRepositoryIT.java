package com.example.uploads;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.uploads.upload_repository.*;
import com.example.uploads.upload_variant_repository.*;
import com.example.users.repository.InsertUser;
import com.example.users.repository.UserRepository;
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
    @Autowired
    private UserRepository userRepository;

    private UploadId initUpload() {
        InsertUser insertUser = new InsertUser();
        var user = userRepository.create(insertUser);

        InsertUpload upload = new InsertUpload(
                user.id(),
                "bucket",
                MediaType.IMAGE,
                "group",
                0,
                UploadStatus.UPLOADING,
                "jpg"
        );
        return uploadRepo.create(upload);
    }

    private UploadVariantId initVariant(){
        var uploadId = initUpload();
        var insert = new InsertUploadVariant(
                new UploadVariantId(uploadId, "variant"),
                "bucket",
                "jpg",
                UploadVariantStatus.READY
        );
        return uploadVariantRepo.create(insert);
    }

    /**
     * The created upload variant should return its id
     */
    @Test
    void testUploadCreation() {
        var variantId = initVariant();
        assertThat(variantId).isNotNull();
    }

    /** The found upload variant should have the same fields as the inserted one */
    @Test
    void testUploadVariantRead(){
        var uploadId = initUpload();

        var insert = new InsertUploadVariant(
                new UploadVariantId(uploadId, "variant"),
                "bucket",
                "jpg",
                UploadVariantStatus.READY
        );
        var variantId = uploadVariantRepo.create(insert);
        var found = uploadVariantRepo.getById(variantId);

        assertThat(found).isNotNull();
        assertThat(found.key().originId()).isEqualTo(uploadId);
        assertThat(found.bucketName()).isEqualTo(insert.bucketName());
        assertThat(found.status()).isEqualTo(insert.status());
        assertThat(found.fileExtension()).isEqualTo(insert.fileExtension());

        // Check generated fields
        assertThat(found.createdAt()).isNotNull();
    }

    /** The all fields should be updated */
    @Test
    void testUploadVariantUpdate(){
        var id = initVariant();
        var original = uploadVariantRepo.getById(id);
        assertThat(original).isNotNull();

        var update = new UploadVariant(
                original.key(),
                "bucket 2",
                "extension 2",
                original.createdAt().plus(1, ChronoUnit.DAYS),
                UploadVariantStatus.CREATING
        );
        uploadVariantRepo.update(update);
        var updated = uploadVariantRepo.getById(id);
        assertThat(updated).isEqualTo(update);
    }

    /** The upload variant should create a foreign key error if the upload does not exist */
    @Test
    void testUploadForeignKey() {
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
