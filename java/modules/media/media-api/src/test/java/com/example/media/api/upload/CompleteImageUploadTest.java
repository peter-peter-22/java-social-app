package com.example.media.api.upload;

import com.example.media.contract.asset.*;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.object.ObjectPrefix;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.transformation.*;
import com.example.media.contract.upload.*;
import com.example.media.contract.variant.NewVariant;
import com.example.media.contract.variant.VariantId;
import com.example.media.contract.worker.CompletedImageOutput;
import com.example.media.contract.worker.ImageTransformResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CompleteImageUploadTest {

    private final UploadRegistry uploadRegistry = mock();
    private final AssetStore assetStore = mock();
    private final TransformationCatalog transformationCatalog = mock();
    private final ObjectStore objectStore = mock();
    private final CompleteImageUpload useCase = new CompleteImageUpload(
            uploadRegistry, assetStore, transformationCatalog, objectStore,
            new TransactionTemplate(mock(PlatformTransactionManager.class)));

    private static final UploadKey KEY = new UploadKey("cover");
    private static final AssetPlacement PLACEMENT = new AssetPlacement("eu-west", "uploads");
    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final AssetId ORIGINAL_ID = AssetId.generate();
    private static final AssetId INCOMING_ID = AssetId.generate();
    private static final AssetId EAGER_ID = AssetId.generate();
    private static final NamedTransformation PROFILE = new NamedTransformation(
            new NamedTransformationId(UUID.randomUUID()), "profile",
            new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(100)),
            new TransformationVersionId(UUID.randomUUID()));
    private static final ImageMetadata SOURCE_METADATA = new ImageMetadata(400, 300);

    private void stubRegistry() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(new UploadAttempt(
                ATTEMPT_ID, KEY, ORIGINAL_ID, List.of("profile"), "optimize",
                UploadAttemptStatus.PENDING, Instant.now(), null)));
        when(transformationCatalog.resolveAll(List.of("profile"))).thenReturn(List.of(PROFILE));
        when(objectStore.homePlacement()).thenReturn(PLACEMENT);
        when(assetStore.findAsset(ORIGINAL_ID)).thenReturn(Optional.of(new Asset(
                ORIGINAL_ID, PLACEMENT, AssetStatus.UPLOADING, AssetDeliveryProtocol.IMAGE,
                "image/jpeg", "original.jpg", "jpg", false, 3, List.of("original.jpg"),
                null, null, Instant.now())));
    }

    @Test
    void finalizesTheUploadOfAnIncomingAndAnEagerResult() {
        stubRegistry();
        var incomingOutput = new CompletedImageOutput(
                INCOMING_ID, prefix(INCOMING_ID), "original.webp", List.of("original.webp"),
                10, "image/webp", new ImageMetadata(100, 50));
        var eagerOutput = new CompletedImageOutput(
                EAGER_ID, prefix(EAGER_ID), "profile.webp", List.of("profile.webp"),
                20, "image/webp", new ImageMetadata(50, 50));
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(incomingOutput, eagerOutput), SOURCE_METADATA);
        var upload = new Upload(new UploadId(UUID.randomUUID()), KEY, INCOMING_ID, Instant.now());
        when(uploadRegistry.registerUpload(any())).thenReturn(upload);

        var done = useCase.complete(ATTEMPT_ID, result);

        assertThat(done).isSameAs(upload);

        // the result assets are registered with the worker's data and metadata
        verify(assetStore).registerAsset(argThatNewAsset(INCOMING_ID, "original.webp", List.of("original.webp"), 10));
        verify(assetStore).registerAsset(argThatNewAsset(EAGER_ID, "profile.webp", List.of("profile.webp"), 20));
        verify(assetStore).setImageMetadata(INCOMING_ID, new ImageMetadata(100, 50));
        verify(assetStore).setImageMetadata(EAGER_ID, new ImageMetadata(50, 50));
        verify(assetStore).setImageMetadata(ORIGINAL_ID, SOURCE_METADATA);

        // the kept assets finalize; the replaced original is left out
        var kept = List.of(INCOMING_ID, EAGER_ID);
        var refs = ArgumentCaptor.<List<ObjectRef>>captor();
        InOrder inOrder = inOrder(assetStore, objectStore, uploadRegistry);
        inOrder.verify(assetStore).setStatus(kept, AssetStatus.FINALIZING);
        inOrder.verify(objectStore).setLifetime(refs.capture(), eq(ObjectLifetime.PERMANENT));
        assertThat(refs.getValue()).containsExactlyInAnyOrder(
                prefix(INCOMING_ID).resolve("original.webp"),
                prefix(EAGER_ID).resolve("profile.webp"));
        inOrder.verify(assetStore).setStatus(kept, AssetStatus.READY);
        inOrder.verify(uploadRegistry).registerUpload(new NewUpload(KEY, ATTEMPT_ID, INCOMING_ID));
        inOrder.verify(uploadRegistry).registerVariants(List.of(new NewVariant(
                new VariantId(upload.id(), PROFILE.id()), PROFILE.currentVersionId(), EAGER_ID)));
        inOrder.verify(uploadRegistry).finishAttempt(ATTEMPT_ID, AttemptOutcome.SUCCESS);
    }

    @Test
    void keepsTheOriginalWithoutAnIncomingResult() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(new UploadAttempt(
                ATTEMPT_ID, KEY, ORIGINAL_ID, List.of("profile"), null,
                UploadAttemptStatus.PENDING, Instant.now(), null)));
        when(transformationCatalog.resolveAll(List.of("profile"))).thenReturn(List.of(PROFILE));
        when(objectStore.homePlacement()).thenReturn(PLACEMENT);
        var originalAsset = new Asset(ORIGINAL_ID, PLACEMENT, AssetStatus.UPLOADING, AssetDeliveryProtocol.IMAGE,
                "image/jpeg", "original.jpg", "jpg", false, 3, List.of("original.jpg"), null, null, Instant.now());
        when(assetStore.findAsset(ORIGINAL_ID)).thenReturn(Optional.of(originalAsset));
        var eagerOutput = new CompletedImageOutput(
                EAGER_ID, prefix(EAGER_ID), "profile.webp", List.of("profile.webp"),
                20, "image/webp", new ImageMetadata(50, 50));
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(eagerOutput), null);
        var upload = new Upload(new UploadId(UUID.randomUUID()), KEY, ORIGINAL_ID, Instant.now());
        when(uploadRegistry.registerUpload(any())).thenReturn(upload);

        useCase.complete(ATTEMPT_ID, result);

        var kept = List.of(ORIGINAL_ID, EAGER_ID);
        verify(assetStore).setStatus(kept, AssetStatus.FINALIZING);
        verify(assetStore).setStatus(kept, AssetStatus.READY);
        verify(uploadRegistry).registerUpload(new NewUpload(KEY, ATTEMPT_ID, ORIGINAL_ID));
        // the original's registered object becomes permanent along with the variant's
        var refs = ArgumentCaptor.<List<ObjectRef>>captor();
        verify(objectStore).setLifetime(refs.capture(), eq(ObjectLifetime.PERMANENT));
        assertThat(refs.getValue()).containsExactlyInAnyOrder(
                originalAsset.prefix().resolve("original.jpg"),
                prefix(EAGER_ID).resolve("profile.webp"));
    }

    @Test
    void completionIsIdempotentWhenTheAttemptAlreadySucceeded() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(new UploadAttempt(
                ATTEMPT_ID, KEY, ORIGINAL_ID, List.of("profile"), "optimize",
                UploadAttemptStatus.SUCCESS, Instant.now(), Instant.now())));
        var upload = new Upload(new UploadId(UUID.randomUUID()), KEY, ORIGINAL_ID, Instant.now());
        when(uploadRegistry.findUpload(KEY)).thenReturn(Optional.of(upload));

        var done = useCase.complete(ATTEMPT_ID, new ImageTransformResult(ATTEMPT_ID, List.of(), null));

        assertThat(done).isSameAs(upload);
        verifyNoInteractions(assetStore, objectStore, transformationCatalog);
    }

    @Test
    void rejectsCompletionOfAFailedAttempt() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(new UploadAttempt(
                ATTEMPT_ID, KEY, ORIGINAL_ID, List.of("profile"), "optimize",
                UploadAttemptStatus.FAILED, Instant.now(), Instant.now())));

        assertThatThrownBy(() -> useCase.complete(
                ATTEMPT_ID, new ImageTransformResult(ATTEMPT_ID, List.of(), null)))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(assetStore, objectStore, transformationCatalog);
    }

    @Test
    void reusesAnIdenticalUploadingAssetWhenTheCompletionIsRetried() {
        stubRegistry();
        var incomingOutput = new CompletedImageOutput(
                INCOMING_ID, prefix(INCOMING_ID), "original.webp", List.of("original.webp"),
                10, "image/webp", new ImageMetadata(100, 50));
        var eagerOutput = new CompletedImageOutput(
                EAGER_ID, prefix(EAGER_ID), "profile.webp", List.of("profile.webp"),
                20, "image/webp", new ImageMetadata(50, 50));
        when(assetStore.findAsset(INCOMING_ID)).thenReturn(Optional.of(new Asset(
                INCOMING_ID, PLACEMENT, AssetStatus.UPLOADING, AssetDeliveryProtocol.IMAGE,
                "image/webp", "original.webp", "webp", false, 10, List.of("original.webp"),
                new ImageMetadata(100, 50), null, Instant.now())));
        when(assetStore.findAsset(EAGER_ID)).thenReturn(Optional.of(new Asset(
                EAGER_ID, PLACEMENT, AssetStatus.UPLOADING, AssetDeliveryProtocol.IMAGE,
                "image/webp", "profile.webp", "webp", false, 20, List.of("profile.webp"),
                new ImageMetadata(50, 50), null, Instant.now())));
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(incomingOutput, eagerOutput), SOURCE_METADATA);
        var upload = new Upload(new UploadId(UUID.randomUUID()), KEY, INCOMING_ID, Instant.now());
        when(uploadRegistry.registerUpload(any())).thenReturn(upload);

        useCase.complete(ATTEMPT_ID, result);

        verify(assetStore, never()).registerAsset(any());
        verify(uploadRegistry).finishAttempt(ATTEMPT_ID, AttemptOutcome.SUCCESS);
    }

    @Test
    void rejectsAResultUnderAnUnexpectedPlacement() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(new UploadAttempt(
                ATTEMPT_ID, KEY, ORIGINAL_ID, List.of("profile"), null,
                UploadAttemptStatus.PENDING, Instant.now(), null)));
        when(transformationCatalog.resolveAll(List.of("profile"))).thenReturn(List.of(PROFILE));
        when(objectStore.homePlacement()).thenReturn(PLACEMENT);
        var id = AssetId.generate();
        var output = new CompletedImageOutput(
                id,
                new ObjectPrefix("another-region", "uploads", id.value().toString()),
                "profile.webp",
                List.of("profile.webp"),
                20,
                "image/webp",
                new ImageMetadata(50, 50));
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(output), null);

        assertThatThrownBy(() -> useCase.complete(ATTEMPT_ID, result))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(assetStore);
    }

    @Test
    void missingAttemptIsNotFound() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.complete(
                ATTEMPT_ID, new ImageTransformResult(ATTEMPT_ID, List.of(), null)))
                .isInstanceOf(UploadNotFoundException.class);
        verifyNoInteractions(assetStore, objectStore, transformationCatalog);
    }

    @Test
    void rejectsAResultOfAnotherAttempt() {
        stubRegistry();

        assertThatThrownBy(() -> useCase.complete(
                ATTEMPT_ID, new ImageTransformResult(new AttemptId(UUID.randomUUID()), List.of(), null)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(assetStore, objectStore);
    }

    @Test
    void rejectsAResultWithUnexpectedOutputCount() {
        stubRegistry();

        // the attempt planned the incoming and the eager result, but the worker reported none
        assertThatThrownBy(() -> useCase.complete(
                ATTEMPT_ID, new ImageTransformResult(ATTEMPT_ID, List.of(), null)))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(assetStore, objectStore);
    }

    @Test
    void failsWhenTheObjectsCannotBeMadePermanent() {
        stubRegistry();
        // The store fails the lifetime change when an object disappeared during the upload.
        doThrow(new RuntimeException("the object disappeared")).when(objectStore)
                .setLifetime(any(), eq(ObjectLifetime.PERMANENT));
        var incomingOutput = new CompletedImageOutput(
                INCOMING_ID, prefix(INCOMING_ID), "original.webp", List.of("original.webp"),
                10, "image/webp", new ImageMetadata(100, 50));
        var eagerOutput = new CompletedImageOutput(
                EAGER_ID, prefix(EAGER_ID), "profile.webp", List.of("profile.webp"),
                20, "image/webp", new ImageMetadata(50, 50));
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(incomingOutput, eagerOutput), null);

        assertThatThrownBy(() -> useCase.complete(ATTEMPT_ID, result))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("the object disappeared");
        verify(assetStore, never()).setStatus(any(), eq(AssetStatus.READY));
        verify(uploadRegistry, never()).registerUpload(any());
        verify(uploadRegistry, never()).finishAttempt(ATTEMPT_ID, AttemptOutcome.SUCCESS);
    }

    private static ObjectPrefix prefix(AssetId id) {
        return new ObjectPrefix("eu-west", "uploads", id.value().toString());
    }

    private static NewAsset argThatNewAsset(AssetId id, String entryPoint, List<String> objects, long bytes) {
        return org.mockito.ArgumentMatchers.argThat(asset -> asset.id().equals(id)
                && asset.entryPoint().equals(entryPoint)
                && asset.objects().equals(objects)
                && asset.bytes() == bytes);
    }
}