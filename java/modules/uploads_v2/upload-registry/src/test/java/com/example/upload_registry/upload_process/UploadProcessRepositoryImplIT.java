package com.example.upload_registry.upload_process;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.uploads_api.stored_asset.AssetDeliveryProtocol;
import com.example.uploads_api.stored_asset.InsertStoredAsset;
import com.example.uploads_api.stored_asset.StoredAsset;
import com.example.uploads_api.stored_asset.StoredAssetRepository;
import com.example.uploads_api.stored_asset.StoredAssetStatus;
import com.example.uploads_api.transformations.ImageEncodings;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.named_transformation_repository.InsertNamedImageTransformation;
import com.example.uploads_api.transformations.named_transformation_repository.NamedTransformationRepository;
import com.example.uploads_api.uploads.upload_process.AttemptNotActiveException;
import com.example.uploads_api.uploads.upload_process.InsertUploadAttempt;
import com.example.uploads_api.uploads.upload_process.UploadAlreadyExistsException;
import com.example.uploads_api.uploads.upload_process.UploadAttemptStatus;
import com.example.uploads_api.uploads.upload_process.UploadProcessRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UploadProcessRepositoryImplIT extends CockroachIntegrationTest {
    @Autowired
    private UploadProcessRepository uploadProcessRepository;
    @Autowired
    private UploadProcessPersistence uploadProcessPersistence;
    @Autowired
    private StoredAssetRepository storedAssetRepository;
    @Autowired
    private NamedTransformationRepository namedTransformationRepository;

    @Test
    void createsProcessOnceAndFinalizesTheActiveAttempt() {
        var uploadKey = "process-" + UUID.randomUUID();
        var first = uploadProcessRepository.createProcessIfNotExistsThenLock(uploadKey);
        var second = uploadProcessRepository.createProcessIfNotExistsThenLock(uploadKey);
        assertThat(second).usingRecursiveComparison().isEqualTo(first);

        var original = insertAsset();
        var variantAsset = insertAsset();
        var transformationName = "thumb-" + UUID.randomUUID().toString().substring(0, 8);
        namedTransformationRepository.set(new InsertNamedImageTransformation(transformationName, ImageTransformationOperations.builder()
                .encoding(ImageEncodings.Jpeg.builder().quality(80).build())
                .build()));
        var transformation = namedTransformationRepository.get(transformationName);

        var attemptId = uploadProcessRepository.createAttempt(InsertUploadAttempt.builder()
                .uploadKey(uploadKey)
                .eagerTransformationNames(List.of(transformationName))
                .originalAssetId(original.getId())
                .build());
        uploadProcessRepository.setAttemptAsActive(uploadKey, attemptId);

        var upload = uploadProcessRepository.finalizeAttempt(
                attemptId,
                uploadKey,
                List.of(new UploadProcessRepository.RegisterVariantArgs(transformation, variantAsset.getId())),
                original.getId());

        assertThat(upload.getUploadKey()).isEqualTo(uploadKey);
        assertThat(upload.getStoredAsset().getId()).isEqualTo(original.getId());
        assertThat(upload.getStoredAsset().getStatus()).isEqualTo(StoredAssetStatus.READY);
        assertThat(storedAssetRepository.findById(variantAsset.getId()).getStatus()).isEqualTo(StoredAssetStatus.READY);
        assertThat(uploadProcessPersistence.findProcess(uploadKey).getUploadId()).isEqualTo(upload.getId());
        assertThat(uploadProcessPersistence.findAttempt(attemptId).getStatus()).isEqualTo(UploadAttemptStatus.SUCCESS);
        assertThat(uploadProcessPersistence.findAttempt(attemptId).getFinishedAt()).isNotNull();

        assertThatThrownBy(() -> uploadProcessRepository.setAttemptAsActive(uploadKey, attemptId))
                .isInstanceOf(UploadAlreadyExistsException.class);
        assertThatThrownBy(() -> uploadProcessRepository.finalizeAttempt(
                attemptId, uploadKey, null, insertAsset().getId()))
                .isInstanceOf(UploadAlreadyExistsException.class);
    }

    @Test
    void rejectsFinalizingAnInactiveAttempt() {
        var uploadKey = "inactive-" + UUID.randomUUID();
        uploadProcessRepository.createProcessIfNotExistsThenLock(uploadKey);
        var original = insertAsset();
        var attemptId = uploadProcessRepository.createAttempt(InsertUploadAttempt.builder()
                .uploadKey(uploadKey)
                .originalAssetId(original.getId())
                .build());

        assertThatThrownBy(() -> uploadProcessRepository.finalizeAttempt(attemptId, uploadKey, null, original.getId()))
                .isInstanceOf(AttemptNotActiveException.class);
        assertThat(uploadProcessPersistence.findProcess(uploadKey).getUploadId()).isNull();
        assertThat(storedAssetRepository.findById(original.getId()).getStatus()).isEqualTo(StoredAssetStatus.UPLOADING);
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
