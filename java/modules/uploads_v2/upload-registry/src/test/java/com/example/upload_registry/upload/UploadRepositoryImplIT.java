package com.example.upload_registry.upload;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.upload_registry.stored_asset.ImageMetadataCrudRepository;
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
    @Autowired
    private ImageMetadataCrudRepository  imageMetadataCrudRepository;

    @Test
    void insertsAndFindsUpload() {
        var upload = uploadRepository.insert(insertUpload("original"));

        var found = uploadRepository.findByKey(upload.getUploadKey());

        assertThat(found).isEqualTo(upload);
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void findsImageUpload() {
        // we need a proper image metadata repository before this
//        var insert = insertUpload("image");
//        var upload = uploadRepository.insert(insert);
//        var imageMetadata=ImageMetadata.builder().resolutionX(1920).resolutionY(1080).build();
//
//        assertThat(uploadRepository.findImageByKey(upload.getUploadKey()).getStoredAsset().getImageMetadata())
//                .isEqualTo(imageMetadata);
    }

    @Test
    void updatesStatus() {
        var upload = uploadRepository.insert(insertUpload("status"));

        uploadRepository.updateStatus(upload.getId(), UploadStatus.UPLOADED);

        assertThat(uploadRepository.findByKey(upload.getUploadKey()).getStatus()).isEqualTo(UploadStatus.UPLOADED);
    }

    @Test
    void duplicateKeyThrowsException() {
        var insert = insertUpload("duplicate");
        uploadRepository.insert(insert);

        assertThatThrownBy(() -> uploadRepository.insert(insert))
                .isInstanceOf(UploadKeyTakenException.class);
    }

    @Test
    void deletesUploadByKey() {
        var upload = uploadRepository.insert(insertUpload("delete"));

        uploadRepository.deleteById(upload.getUploadKey());

        assertThatThrownBy(() -> uploadRepository.findByKey(upload.getUploadKey()))
                .isInstanceOf(java.util.NoSuchElementException.class);
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
