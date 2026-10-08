package com.example.upload_registry.upload;

import com.example.cockroach_db.CockroachIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UploadRepositoryIT extends CockroachIntegrationTest {
    @Autowired
    private UploadRepository uploadRepository;
    @Autowired
    private ImageMetadataRepository imageMetadataRepository;

    private UploadEntity insertUpload() {
        return uploadRepository.save(
                UploadEntity.builder()
                        .uploadKey("my_image" + UUID.randomUUID())
                        .homeRegion("eu-1")
                        .mimeType("image/jpg")
                        .bytes(1000)
                        .build()
        );
    }

    @Test
    void theInsertedUploadShouldBeSaved() {
        var insertedUpload = insertUpload();
        var foundUpload = uploadRepository.getByUploadKey(insertedUpload.getUploadKey());
        assertThat(foundUpload.isPresent()).isTrue();
        assertThat(foundUpload.get().getUploadKey()).isEqualTo(insertedUpload.getUploadKey());
    }

    @Test
    void theInsertedImageMetadataShouldBeSaved() {
        var insertedUpload = insertUpload();
        var referenceUpload = uploadRepository.getReferenceById(insertedUpload.getInternalId());
        var insertedMetadata = imageMetadataRepository.save(
                ImageMetadataEntity.builder()
                        .upload(referenceUpload)
                        .resolutionX(1920)
                        .resolutionY(1080)
                        .build()
        );
        var foundUpload = uploadRepository.getByUploadKeyWithImageMetadata(insertedUpload.getUploadKey());

        assertThat(foundUpload.isPresent()).isTrue();
        assertThat(foundUpload.get().imageMetadata.getUploadId()).isEqualTo(insertedMetadata.getUploadId());
    }
}
