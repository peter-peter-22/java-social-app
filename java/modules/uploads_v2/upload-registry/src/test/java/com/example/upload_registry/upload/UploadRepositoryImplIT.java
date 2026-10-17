package com.example.upload_registry.upload;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.uploads_api.stored_asset.ImageMetadata;
import com.example.uploads_api.stored_asset.InsertStoredAsset;
import com.example.uploads_api.stored_asset.StoredAssetRepository;
import com.example.uploads_api.stored_asset.VideoMetadata;
import com.example.uploads_api.uploads.InsertUpload;
import com.example.uploads_api.uploads.Upload;
import com.example.uploads_api.uploads.UploadKeyTakenException;
import com.example.uploads_api.uploads.UploadStatus;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UploadRepositoryImplIT extends CockroachIntegrationTest {
    @Autowired
    private UploadRepositoryImpl uploadRepository;
    @Autowired
    private StoredAssetRepository storedAssetRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void insertsAndFindsUpload() {
        var toInsert = insertUpload("original");
        var uploadId = uploadRepository.insert(toInsert);
        var found = uploadRepository.findByKey(toInsert.getUploadKey());

        assertThat(found.getId()).isEqualTo(uploadId);
        assertThat(found.getUploadKey()).isEqualTo(toInsert.getUploadKey());
        assertThat(found).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "storedAsset")
                .isEqualTo(toInsert);
        assertThat(found.getStoredAsset()).isEqualTo(storedAssetRepository.findById(toInsert.getStoredAssetId()));
    }

    @Test
    void findsImageUpload() {
        var toInsert = insertUpload("image");
        var imageMetadata = ImageMetadata.builder().resolutionX(1920).resolutionY(1080).build();
        uploadRepository.insert(toInsert);
        storedAssetRepository.setImageMetadata(toInsert.getStoredAssetId(), imageMetadata);
        storedAssetRepository.setVideoMetadata(toInsert.getStoredAssetId(), VideoMetadata.builder()
                .resolutionX(1280).resolutionY(720).durationMilliseconds(1_000).build());

        var found = findUsingOneStatement(() -> uploadRepository.findImageByKey(toInsert.getUploadKey()));

        assertThat(found.getStoredAsset().getImageMetadata()).isEqualTo(imageMetadata);
        assertThat(found.getStoredAsset().getVideoMetadata()).isNull();

        var withoutMetadata = findUsingOneStatement(() -> uploadRepository.findByKey(toInsert.getUploadKey()));
        assertThat(withoutMetadata.getStoredAsset().getImageMetadata()).isNull();
        assertThat(withoutMetadata.getStoredAsset().getVideoMetadata()).isNull();
    }

    @Test
    void findsVideoUpload() {
        var toInsert = insertUpload("video", "video/mp4", "mp4");
        var videoMetadata = VideoMetadata.builder()
                .resolutionX(1920)
                .resolutionY(1080)
                .durationMilliseconds(1_000)
                .build();
        uploadRepository.insert(toInsert);
        storedAssetRepository.setVideoMetadata(toInsert.getStoredAssetId(), videoMetadata);
        storedAssetRepository.setImageMetadata(toInsert.getStoredAssetId(), ImageMetadata.builder()
                .resolutionX(1280).resolutionY(720).build());

        var found = findUsingOneStatement(() -> uploadRepository.findVideoByKey(toInsert.getUploadKey()));

        assertThat(found.getStoredAsset().getVideoMetadata()).isEqualTo(videoMetadata);
        assertThat(found.getStoredAsset().getImageMetadata()).isNull();
    }

    @Test
    void updatesStatus() {
        var toInsert = insertUpload("status");
        var uploadId = uploadRepository.insert(toInsert);

        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        var statementsBefore = statistics.getPrepareStatementCount();
        try {
            uploadRepository.updateStatus(uploadId, UploadStatus.UPLOADED);
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(statementsBefore + 1);
        } finally {
            statistics.setStatisticsEnabled(false);
        }

        assertThat(uploadRepository.findByKey(toInsert.getUploadKey()).getStatus()).isEqualTo(UploadStatus.UPLOADED);
    }

    @Test
    void updatingMissingUploadThrows() {
        assertThatThrownBy(() -> uploadRepository.updateStatus(UUID.randomUUID(), UploadStatus.UPLOADED))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void duplicateKeyThrowsException() {
        var toInsert = insertUpload("duplicate");
        uploadRepository.insert(toInsert);

        assertThatThrownBy(() -> uploadRepository.insert(toInsert))
                .isInstanceOf(UploadKeyTakenException.class);
    }

    private Upload findUsingOneStatement(Supplier<Upload> lookup) {
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        var statementsBefore = statistics.getPrepareStatementCount();
        try {
            var found = lookup.get();
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(statementsBefore + 1);
            return found;
        } finally {
            statistics.setStatisticsEnabled(false);
        }
    }

    private InsertUpload insertUpload(String prefix) {
        return insertUpload(prefix, "image/jpeg", "jpg");
    }

    private InsertUpload insertUpload(String prefix, String mimeType, String extension) {
        var storedAssetId = storedAssetRepository.insert(InsertStoredAsset.builder()
                .homeRegion("eu-central-1")
                .objectBucket("uploads")
                .objectPath("assets/" + UUID.randomUUID() + "." + extension)
                .directory(false)
                .mimeType(mimeType)
                .extension(extension)
                .bytes(42)
                .deliveryProtocol("https")
                .build());
        return InsertUpload.builder()
                .uploadKey(prefix + "-" + UUID.randomUUID())
                .status(UploadStatus.UPLOADING)
                .storedAssetId(storedAssetId)
                .build();
    }
}
