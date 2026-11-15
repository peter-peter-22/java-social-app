package com.example.upload_registry.upload_process;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.upload_registry.upload.UploadRepositoryImpl;
import com.example.uploads_api.stored_asset.AssetDeliveryProtocol;
import com.example.uploads_api.stored_asset.InsertStoredAsset;
import com.example.uploads_api.stored_asset.StoredAsset;
import com.example.uploads_api.stored_asset.StoredAssetRepository;
import com.example.uploads_api.uploads.InsertUpload;
import com.example.uploads_api.uploads.upload_process.UploadAttempt;
import com.example.uploads_api.uploads.upload_process.UploadAttemptStatus;
import com.example.uploads_api.uploads.upload_process.UploadProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UploadProcessPersistenceImplIT extends CockroachIntegrationTest {
    @Autowired
    private UploadProcessPersistence uploadProcessPersistence;
    @Autowired
    private StoredAssetRepository storedAssetRepository;
    @Autowired
    private UploadRepositoryImpl uploadRepository;

    @Test
    void savesAndReadsProcessWithoutActiveAttempt() {
        var uploadKey = uploadKey();

        var saved = uploadProcessPersistence.save(UploadProcess.builder()
                .uploadKey(uploadKey)
                .build());

        assertThat(uploadProcessPersistence.findProcess(uploadKey))
                .usingRecursiveComparison()
                .isEqualTo(saved);
        assertThat(saved.getActiveAttemptId()).isNull();
        assertThat(saved.getUploadId()).isNull();
    }

    @Test
    void savesAndReadsAttemptAndActiveProcess() {
        var uploadKey = uploadKey();
        uploadProcessPersistence.save(UploadProcess.builder()
                .uploadKey(uploadKey)
                .build());
        var originalAsset = insertAsset();
        var eagerTransformations = List.of("thumbnail", "preview");
        var finishedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

        var savedAttempt = uploadProcessPersistence.save(UploadAttempt.builder()
                .uploadKey(uploadKey)
                .eagerTransformationNames(eagerTransformations)
                .incomingTransformationName("incoming")
                .originalAssetId(originalAsset.getId())
                .status(UploadAttemptStatus.PENDING)
                .finishedAt(finishedAt)
                .build());
        var savedProcess = uploadProcessPersistence.save(UploadProcess.builder()
                .uploadKey(uploadKey)
                .activeAttemptId(savedAttempt.getAttemptId())
                .build());

        assertThat(savedAttempt.getAttemptId()).isNotNull();
        assertThat(savedAttempt.getCreatedAt()).isNotNull();
        assertThat(uploadProcessPersistence.findAttempt(savedAttempt.getAttemptId()))
                .usingRecursiveComparison()
                .ignoringFields("createdAt")
                .isEqualTo(UploadAttempt.builder()
                        .attemptId(savedAttempt.getAttemptId())
                        .uploadKey(uploadKey)
                        .eagerTransformationNames(eagerTransformations)
                        .incomingTransformationName("incoming")
                        .originalAssetId(originalAsset.getId())
                        .status(UploadAttemptStatus.PENDING)
                        .finishedAt(finishedAt)
                        .build());
        assertThat(uploadProcessPersistence.findProcess(uploadKey))
                .usingRecursiveComparison()
                .isEqualTo(savedProcess);
        assertThat(savedProcess.getActiveAttemptId()).isEqualTo(savedAttempt.getAttemptId());
        assertThat(savedProcess.getUploadId()).isNull();
    }

    @Test
    void savesProcessPointingAtFinishedUpload() {
        var uploadKey = uploadKey();
        var asset = insertAsset();
        var uploadId = uploadRepository.insert(InsertUpload.builder()
                .uploadKey(uploadKey)
                .storedAssetId(asset.getId())
                .build());

        var saved = uploadProcessPersistence.save(UploadProcess.builder()
                .uploadKey(uploadKey)
                .uploadId(uploadId)
                .build());

        assertThat(uploadProcessPersistence.findProcess(uploadKey))
                .usingRecursiveComparison()
                .isEqualTo(saved);
        assertThat(saved.getUploadId()).isEqualTo(uploadId);
    }

    private String uploadKey() {
        return "process-" + UUID.randomUUID();
    }

    private StoredAsset insertAsset() {
        return storedAssetRepository.insert(InsertStoredAsset.builder()
                .homeRegion("eu-central-1")
                .objectBucket("uploads")
                .directory(false)
                .entryPoint("original.jpg")
                .contentType("image/jpeg")
                .extension("jpg")
                .bytes(42)
                .objects(List.of("original.jpg"))
                .deliveryProtocol(AssetDeliveryProtocol.IMAGE)
                .build());
    }
}
