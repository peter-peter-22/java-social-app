package com.example.upload_registry.upload;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.upload_registry.stored_asset.StoredAssetCrudRepository;
import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.uploads_api.uploads.InsertUpload;
import com.example.uploads_api.uploads.UploadKeyTakenException;
import com.example.uploads_api.uploads.UploadStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UploadRepositoryImplIT extends CockroachIntegrationTest {
    @Autowired
    private UploadRepositoryImpl uploadRepository;
    @Autowired
    private StoredAssetCrudRepository storedAssetCrudRepository;

    @Test
    void insertsAndFindsUpload() {
        var toInsert = insertUpload("original");
        var upload = uploadRepository.insert(toInsert);

        var found = uploadRepository.findByKey(toInsert.getUploadKey());

        assertThat(found).isEqualTo(upload);
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void findsImageUpload() {

    }

    @Test
    void findsVideoUpload() {

    }

    @Test
    void updatesStatus() {
        var toInsert = insertUpload("status");
        var uploadId = uploadRepository.insert(toInsert);

        uploadRepository.updateStatus(uploadId, UploadStatus.UPLOADED);

        assertThat(uploadRepository.findByKey(toInsert.getUploadKey()).getStatus()).isEqualTo(UploadStatus.UPLOADED);
    }

    @Test
    void duplicateKeyThrowsException() {
        var toInsert = insertUpload("duplicate");
        uploadRepository.insert(toInsert);

        assertThatThrownBy(() -> uploadRepository.insert(toInsert))
                .isInstanceOf(UploadKeyTakenException.class);
    }

    private InsertUpload insertUpload(String prefix) {
        var storedAsset = storedAssetCrudRepository.save(StoredAssetEntity.builder()
                .homeRegion("eu-central-1")
                .objectBucket("uploads")
                .objectPath("images/" + UUID.randomUUID() + ".jpg")
                .directory(false)
                .entryPoint("index")
                .mimeType("image/jpeg")
                .extension("jpg")
                .bytes(42)
                .deliveryProtocol("https")
                .build());
        return InsertUpload.builder()
                .uploadKey(prefix + "-" + UUID.randomUUID())
                .status(UploadStatus.UPLOADING)
                .storedAssetId(storedAsset.getId())
                .build();
    }
}
