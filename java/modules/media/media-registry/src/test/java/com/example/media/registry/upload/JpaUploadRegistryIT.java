package com.example.media.registry.upload;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.media.contract.asset.*;
import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.transformation.NamedTransformation;
import com.example.media.contract.transformation.NewNamedTransformation;
import com.example.media.contract.upload.*;
import com.example.media.contract.variant.NewVariant;
import com.example.media.contract.variant.VariantId;
import com.example.media.registry.asset.AssetCrudRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JpaUploadRegistryIT extends CockroachIntegrationTest {
    @Autowired
    private UploadRegistry uploadRegistry;
    @Autowired
    private AssetStore assetStore;
    @Autowired
    private TransformationCatalog transformationCatalog;
    @Autowired
    private UploadCrudRepository uploadCrudRepository;
    @Autowired
    private AssetCrudRepository assetCrudRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void opensTheProcessOnceAndReturnsItOnEveryCall() {
        var key = key("process");

        var first = uploadRegistry.openOrGetProcess(key);
        var second = uploadRegistry.openOrGetProcess(key);

        assertThat(second).isEqualTo(first);
        assertThat(first.activeAttemptId()).isNull();
        assertThat(first.uploadId()).isNull();
    }

    @Test
    void createsAnAttemptAndReadsItBack() {
        var key = key("attempt");
        uploadRegistry.openOrGetProcess(key);
        var original = insertAsset();

        var attemptId = uploadRegistry.createAttempt(
                new NewUploadAttempt(
                        key, original, List.of("thumbnail", "preview"), "incoming",
                        UploadVisibility.PRIVATE, null));

        var attempt = uploadRegistry.findAttempt(attemptId).orElseThrow();
        assertThat(attempt.id()).isEqualTo(attemptId);
        assertThat(attempt.key()).isEqualTo(key);
        assertThat(attempt.originalAssetId()).isEqualTo(original);
        assertThat(attempt.eagerTransformationNames()).containsExactly("thumbnail", "preview");
        assertThat(attempt.incomingTransformationName()).isEqualTo("incoming");
        assertThat(attempt.visibility()).isEqualTo(UploadVisibility.PRIVATE);
        assertThat(attempt.status()).isEqualTo(UploadAttemptStatus.PENDING);
        assertThat(attempt.createdAt()).isNotNull();
        assertThat(attempt.finishedAt()).isNull();
        assertThat(uploadRegistry.findAttempt(new AttemptId(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void theCallerCommitsTheFinalizedAttemptAsOneUnit() {
        // The finalize sequence is composed from the port operations inside the caller's
        // transaction: the application service owns the boundary, the registry joins it.
        var fixture = prepareFinalize();
        var template = new TransactionTemplate(transactionManager);

        template.executeWithoutResult(status -> finalizeAttempt(fixture));

        var upload = uploadRegistry.findUpload(fixture.key()).orElseThrow();
        assertThat(upload.assetId()).isEqualTo(fixture.originalAssetId());
        assertThat(upload.visibility()).isEqualTo(fixture.visibility());
        assertThat(uploadRegistry.openOrGetProcess(fixture.key()).uploadId()).isEqualTo(upload.id());
        var attempt = uploadRegistry.findAttempt(fixture.attemptId()).orElseThrow();
        assertThat(attempt.status()).isEqualTo(UploadAttemptStatus.SUCCESS);
        assertThat(attempt.finishedAt()).isNotNull();
        var variant = uploadRegistry.findVariant(fixture.key(), fixture.transformation().name()).orElseThrow();
        assertThat(variant.id().uploadId()).isEqualTo(upload.id());
        assertThat(variant.id().namedTransformationId()).isEqualTo(fixture.transformation().id());
        assertThat(variant.versionId()).isEqualTo(fixture.transformation().currentVersionId());
        assertThat(variant.assetId()).isEqualTo(fixture.variantAssetId());
        assertThat(assetStore.findAsset(fixture.originalAssetId()).orElseThrow().status())
                .isEqualTo(AssetStatus.READY);
        assertThat(assetStore.findAsset(fixture.variantAssetId()).orElseThrow().status())
                .isEqualTo(AssetStatus.READY);
    }

    @Test
    void aFailedCallerRollsBackTheWholeFinalize() {
        var fixture = prepareFinalize();
        var template = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> template.executeWithoutResult(status -> {
            finalizeAttempt(fixture);
            throw new IllegalStateException("the application failed after the writes");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(uploadRegistry.findUpload(fixture.key())).isEmpty();
        assertThat(uploadRegistry.openOrGetProcess(fixture.key()).uploadId()).isNull();
        var attempt = uploadRegistry.findAttempt(fixture.attemptId()).orElseThrow();
        assertThat(attempt.status()).isEqualTo(UploadAttemptStatus.PENDING);
        assertThat(attempt.finishedAt()).isNull();
        assertThat(uploadRegistry.findVariant(fixture.key(), fixture.transformation().name())).isEmpty();
        assertThat(assetStore.findAsset(fixture.originalAssetId()).orElseThrow().status())
                .isEqualTo(AssetStatus.UPLOADING);
        assertThat(assetStore.findAsset(fixture.variantAssetId()).orElseThrow().status())
                .isEqualTo(AssetStatus.UPLOADING);
    }

    @Test
    void aFinishedProcessRejectsNewAttemptsAndUploads() {
        var fixture = prepareFinalize();
        var template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult(status -> finalizeAttempt(fixture));

        assertThatThrownBy(() -> uploadRegistry.activateAttempt(fixture.key(), fixture.attemptId()))
                .isInstanceOf(UploadAlreadyFinishedException.class);
        assertThatThrownBy(() -> uploadRegistry.registerUpload(
                new NewUpload(
                        fixture.key(), fixture.attemptId(), fixture.originalAssetId(), fixture.visibility())))
                .isInstanceOf(AttemptNotActiveException.class);
    }

    @Test
    void rejectsRegisteringAnUploadForAnInactiveAttempt() {
        var key = key("inactive");
        var original = insertAsset();
        uploadRegistry.openOrGetProcess(key);
        var attemptId = uploadRegistry.createAttempt(new NewUploadAttempt(key, original, List.of(), null, UploadVisibility.PUBLIC, null));

        assertThatThrownBy(() -> uploadRegistry.registerUpload(new NewUpload(key, attemptId, original, UploadVisibility.PUBLIC)))
                .isInstanceOf(AttemptNotActiveException.class);

        assertThat(uploadRegistry.findUpload(key)).isEmpty();
        assertThat(uploadRegistry.openOrGetProcess(key).uploadId()).isNull();
        assertThat(assetStore.findAsset(original).orElseThrow().status()).isEqualTo(AssetStatus.UPLOADING);
    }

    @Test
    void rejectsRegisteringAnUploadWhenTheKeyIsTaken() {
        var key = key("taken");
        var original = insertAsset();
        uploadRegistry.openOrGetProcess(key);
        var attemptId = uploadRegistry.createAttempt(new NewUploadAttempt(key, original, List.of(), null, UploadVisibility.PUBLIC, null));
        uploadRegistry.activateAttempt(key, attemptId);
        // A leftover upload row takes the key with another asset and without a linked process.
        uploadCrudRepository.saveAndFlush(UploadEntity.builder()
                .uploadKey(key.value())
                .asset(assetCrudRepository.getReferenceById(insertAsset().value()))
                .visibility(UploadVisibility.PUBLIC)
                .build());

        assertThatThrownBy(() -> uploadRegistry.registerUpload(new NewUpload(key, attemptId, original, UploadVisibility.PUBLIC)))
                .isInstanceOf(UploadKeyTakenException.class);
        assertThat(uploadRegistry.openOrGetProcess(key).uploadId()).isNull();
    }

    @Test
    void finishAttemptRecordsTheFailedOutcome() {
        var key = key("failed");
        var original = insertAsset();
        uploadRegistry.openOrGetProcess(key);
        var attemptId = uploadRegistry.createAttempt(new NewUploadAttempt(key, original, List.of(), null, UploadVisibility.PUBLIC, null));

        uploadRegistry.finishAttempt(attemptId, AttemptOutcome.FAILED);

        var attempt = uploadRegistry.findAttempt(attemptId).orElseThrow();
        assertThat(attempt.status()).isEqualTo(UploadAttemptStatus.FAILED);
        assertThat(attempt.finishedAt()).isNotNull();
    }

    @Test
    void findUploadReturnsEmptyForAnUnknownKey() {
        assertThat(uploadRegistry.findUpload(key("unknown"))).isEmpty();
    }

    /**
     * The finalize the application service will own: the registry operations and the asset status
     * update run in one caller transaction. The order follows the control flow of the plan.
     */
    private void finalizeAttempt(FinalizeFixture fixture) {
        assetStore.setStatus(
                List.of(fixture.originalAssetId(), fixture.variantAssetId()), AssetStatus.READY);
        var upload = uploadRegistry.registerUpload(
                new NewUpload(
                        fixture.key(), fixture.attemptId(), fixture.originalAssetId(), fixture.visibility()));
        uploadRegistry.registerVariants(List.of(new NewVariant(
                new VariantId(upload.id(), fixture.transformation().id()),
                fixture.transformation().currentVersionId(),
                fixture.variantAssetId())));
        uploadRegistry.finishAttempt(fixture.attemptId(), AttemptOutcome.SUCCESS);
    }

    private FinalizeFixture prepareFinalize() {
        var key = key("finalize");
        var original = insertAsset();
        var variantAsset = insertAsset();
        var transformation = transformationCatalog.register(new NewNamedTransformation(
                "thumb-" + UUID.randomUUID().toString().substring(0, 8),
                new ImageTransformationSpec(null, null, null, new ImageEncoding.Jpeg(80))));
        uploadRegistry.openOrGetProcess(key);
        var attemptId = uploadRegistry.createAttempt(
                new NewUploadAttempt(
                        key, original, List.of(transformation.name()), null,
                        UploadVisibility.PUBLIC, null));
        uploadRegistry.activateAttempt(key, attemptId);
        return new FinalizeFixture(
                key, attemptId, original, variantAsset, transformation, UploadVisibility.PUBLIC);
    }

    private record FinalizeFixture(
            UploadKey key,
            AttemptId attemptId,
            AssetId originalAssetId,
            AssetId variantAssetId,
            NamedTransformation transformation,
            UploadVisibility visibility
    ) {
    }

    private UploadKey key(String prefix) {
        return new UploadKey(prefix + "-" + UUID.randomUUID());
    }

    private AssetId insertAsset() {
        Asset asset = assetStore.registerAsset(newAsset(AssetId.generate()));
        return asset.id();
    }

    private NewAsset newAsset(AssetId id) {
        return new NewAsset(
                id,
                new AssetPlacement("eu-central-1", "uploads"),
                AssetDeliveryProtocol.IMAGE,
                "image/jpeg",
                "original.jpg",
                "jpg",
                false,
                42,
                List.of("original.jpg"));
    }
}
