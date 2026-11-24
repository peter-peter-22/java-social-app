package com.example.media.api.upload;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.asset.NewAsset;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.port.*;
import com.example.media.contract.transformation.*;
import com.example.media.contract.upload.*;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StartImageUploadTest {

    private final UploadRegistry uploadRegistry = mock();
    private final AssetStore assetStore = mock();
    private final TransformationCatalog transformationCatalog = mock();
    private final ObjectStore objectStore = mock();
    private final ImageTransformer imageTransformer = mock();
    private final CompleteImageUpload completeImageUpload = mock();
    private final FailImageUpload failImageUpload = mock();
    private final StartImageUpload useCase = new StartImageUpload(
            uploadRegistry,
            assetStore,
            transformationCatalog,
            objectStore,
            imageTransformer,
            completeImageUpload,
            failImageUpload,
            new TransactionTemplate(mock(PlatformTransactionManager.class)));

    private static final UploadKey KEY = new UploadKey("cover");
    private static final AssetPlacement PLACEMENT = new AssetPlacement("eu-west", "uploads");
    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final ImageTransformationSpec OPTIMIZE_SPEC =
            new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50));
    private static final NamedTransformation OPTIMIZE = new NamedTransformation(
            new NamedTransformationId(UUID.randomUUID()), "optimize", OPTIMIZE_SPEC,
            new TransformationVersionId(UUID.randomUUID()));
    private static final NamedTransformation PROFILE = new NamedTransformation(
            new NamedTransformationId(UUID.randomUUID()), "profile",
            new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(100)),
            new TransformationVersionId(UUID.randomUUID()));

    @Test
    void runsTheBlockingUploadAndFinalizes() {
        when(objectStore.homePlacement()).thenReturn(PLACEMENT);
        when(transformationCatalog.resolve("optimize")).thenReturn(OPTIMIZE);
        when(transformationCatalog.resolve("profile")).thenReturn(PROFILE);
        when(uploadRegistry.createAttempt(any())).thenReturn(ATTEMPT_ID);
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(), null);
        when(imageTransformer.transform(any())).thenReturn(result);
        var upload = new Upload(new UploadId(UUID.randomUUID()), KEY, AssetId.generate(), Instant.now());
        when(completeImageUpload.complete(ATTEMPT_ID, result)).thenReturn(upload);

        var started = useCase.start(
                new StartImageUpload.Request("cover", "jpg", null, null, List.of("profile"), "optimize", null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg");

        assertThat(started.async()).isFalse();
        assertThat(started.upload()).isSameAs(upload);

        // the attempt and the original asset are registered in one transaction
        verify(uploadRegistry).openOrGetProcess(KEY);
        var newAsset = ArgumentCaptor.forClass(NewAsset.class);
        verify(assetStore).registerAsset(newAsset.capture());
        assertThat(newAsset.getValue().entryPoint()).isEqualTo("original.jpg");
        assertThat(newAsset.getValue().objects()).containsExactly("original.jpg");
        assertThat(newAsset.getValue().contentType()).isEqualTo("image/jpeg");
        assertThat(newAsset.getValue().placement()).isEqualTo(PLACEMENT);
        verify(uploadRegistry).createAttempt(
                new NewUploadAttempt(KEY, newAsset.getValue().id(), List.of("profile"), "optimize"));
        verify(uploadRegistry).activateAttempt(KEY, ATTEMPT_ID);

        // the original is written temporary, under the reserved prefix
        var ref = ArgumentCaptor.forClass(ObjectRef.class);
        verify(objectStore).put(ref.capture(), any(InputStream.class), eq(3L), eq("image/jpeg"),
                eq(ObjectLifetime.TEMPORARY));
        assertThat(ref.getValue().region()).isEqualTo("eu-west");
        assertThat(ref.getValue().bucket()).isEqualTo("uploads");
        assertThat(ref.getValue().key()).isEqualTo(newAsset.getValue().id().value() + "/original_jpg");

        // the command plans the incoming result first, then the eager result in request order
        var command = ArgumentCaptor.forClass(ImageTransformCommand.class);
        verify(imageTransformer).transform(command.capture());
        assertThat(command.getValue().attemptId()).isEqualTo(ATTEMPT_ID);
        assertThat(command.getValue().source()).isEqualTo(ref.getValue());
        assertThat(command.getValue().outputs()).hasSize(2);
        assertThat(command.getValue().outputs().get(0).isIncoming()).isTrue();
        assertThat(command.getValue().outputs().get(0).placement()).isEqualTo(PLACEMENT);
        assertThat(command.getValue().outputs().get(0).spec()).isEqualTo(OPTIMIZE_SPEC);
        assertThat(command.getValue().outputs().get(1).transformationName()).isEqualTo("profile");
        assertThat(command.getValue().outputs().get(1).placement()).isEqualTo(PLACEMENT);
        assertThat(command.getValue().outputs().get(1).spec()).isEqualTo(PROFILE.spec());

        // the blocking request finalizes before it responds
        verify(completeImageUpload).complete(ATTEMPT_ID, result);
    }

    @Test
    void marksTheAttemptFailedWhenTheWorkerFinallyFails() {
        when(objectStore.homePlacement()).thenReturn(PLACEMENT);
        when(transformationCatalog.resolve("optimize")).thenReturn(OPTIMIZE);
        when(uploadRegistry.createAttempt(any())).thenReturn(ATTEMPT_ID);
        when(imageTransformer.transform(any())).thenThrow(new IllegalStateException("worker down"));

        assertThatThrownBy(() -> useCase.start(
                new StartImageUpload.Request("cover", "jpg", null, null, null, "optimize", null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(IllegalStateException.class);

        verify(failImageUpload).fail(ATTEMPT_ID);
        verify(completeImageUpload, never()).complete(any(), any());
    }

    @Test
    void queuesTheUploadWhenAsyncAndFinalizesLater() {
        when(objectStore.homePlacement()).thenReturn(PLACEMENT);
        when(transformationCatalog.resolve("optimize")).thenReturn(OPTIMIZE);
        when(transformationCatalog.resolve("profile")).thenReturn(PROFILE);
        when(uploadRegistry.createAttempt(any())).thenReturn(ATTEMPT_ID);
        var acceptedAt = Instant.now();
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(new UploadAttempt(
                ATTEMPT_ID, KEY, AssetId.generate(), List.of("profile"), "optimize",
                UploadAttemptStatus.PENDING, acceptedAt, null)));

        var started = useCase.start(
                new StartImageUpload.Request("cover", "jpg", null, null, List.of("profile"), "optimize", true),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg");

        assertThat(started.async()).isTrue();
        assertThat(started.upload()).isNull();
        assertThat(started.acceptedAt()).isEqualTo(acceptedAt);

        var command = ArgumentCaptor.forClass(ImageTransformCommand.class);
        verify(imageTransformer).transformAsync(command.capture());
        assertThat(command.getValue().attemptId()).isEqualTo(ATTEMPT_ID);
        // the source is the original asset's object, under the id reserved at registration
        var newAsset = ArgumentCaptor.forClass(NewAsset.class);
        verify(assetStore).registerAsset(newAsset.capture());
        assertThat(command.getValue().source())
                .isEqualTo(new ObjectRef("eu-west", "uploads", newAsset.getValue().id().value() + "/original_jpg"));
        assertThat(command.getValue().outputs()).hasSize(2);
        // The attempt stays PENDING; no completion and no failure marking happen here.
        verify(completeImageUpload, never()).complete(any(), any());
        verify(failImageUpload, never()).fail(any());
    }

    @Test
    void marksTheAttemptFailedWhenTheAsyncPublishFails() {
        when(objectStore.homePlacement()).thenReturn(PLACEMENT);
        when(transformationCatalog.resolve("optimize")).thenReturn(OPTIMIZE);
        when(uploadRegistry.createAttempt(any())).thenReturn(ATTEMPT_ID);
        doThrow(new IllegalStateException("broker down")).when(imageTransformer).transformAsync(any());

        assertThatThrownBy(() -> useCase.start(
                new StartImageUpload.Request("cover", "jpg", null, null, null, "optimize", true),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(IllegalStateException.class);

        verify(failImageUpload).fail(ATTEMPT_ID);
        verify(completeImageUpload, never()).complete(any(), any());
    }

    @Test
    void withoutTransformationsSkipsTheWorker() {
        when(objectStore.homePlacement()).thenReturn(PLACEMENT);
        when(uploadRegistry.createAttempt(any())).thenReturn(ATTEMPT_ID);
        var upload = new Upload(new UploadId(UUID.randomUUID()), KEY, AssetId.generate(), Instant.now());
        when(completeImageUpload.complete(eq(ATTEMPT_ID), any())).thenReturn(upload);

        var started = useCase.start(
                new StartImageUpload.Request("cover", "jpg", null, null, null, null, null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg");

        assertThat(started.async()).isFalse();
        assertThat(started.upload()).isSameAs(upload);
        verifyNoInteractions(imageTransformer);
        verify(completeImageUpload).complete(eq(ATTEMPT_ID), any());
        verify(uploadRegistry).createAttempt(any());
    }

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> useCase.start(request("cover", "jpg", null, null, List.of("profile"), "optimize"),
                InputStream.nullInputStream(), 0, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(uploadRegistry, assetStore, objectStore, imageTransformer);
    }

    @Test
    void rejectsSizeMismatch() {
        assertThatThrownBy(() -> useCase.start(request("cover", "jpg", null, 4L, null, null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(uploadRegistry, assetStore, objectStore, imageTransformer);
    }

    @Test
    void rejectsContentTypeMismatch() {
        assertThatThrownBy(() -> useCase.start(request("cover", "jpg", "image/png", null, null, null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(uploadRegistry, assetStore, objectStore, imageTransformer);
    }

    @Test
    void rejectsMissingExtension() {
        assertThatThrownBy(() -> useCase.start(request("cover", null, null, null, null, null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(uploadRegistry, assetStore, objectStore, imageTransformer);
    }

    @Test
    void rejectsBlankUploadKey() {
        assertThatThrownBy(() -> useCase.start(request(" ", "jpg", null, null, null, null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(uploadRegistry, assetStore, objectStore, imageTransformer);
    }

    @Test
    void rejectsDuplicateEagerNames() {
        assertThatThrownBy(() -> useCase.start(request("cover", "jpg", null, null, List.of("profile", "profile"), null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(uploadRegistry, assetStore, objectStore, imageTransformer);
    }

    @Test
    void rejectsIncomingRepeatedAsEager() {
        assertThatThrownBy(() -> useCase.start(
                request("cover", "jpg", null, null, List.of("optimize"), "optimize"),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(uploadRegistry, assetStore, objectStore, imageTransformer);
    }

    @Test
    void rejectsUnknownTransformationBeforeAnythingIsWritten() {
        when(transformationCatalog.resolve("missing")).thenThrow(new TransformationNotFoundException("missing"));

        assertThatThrownBy(() -> useCase.start(request("cover", "jpg", null, null, List.of("missing"), null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(TransformationNotFoundException.class);
        verifyNoInteractions(uploadRegistry, assetStore, objectStore, imageTransformer);
    }

    @Test
    void rejectsNonImageTransformation() {
        when(transformationCatalog.resolve("clip")).thenReturn(new NamedTransformation(
                new NamedTransformationId(UUID.randomUUID()), "clip",
                new VideoTransformationSpec(new VideoTransformationSpec.VideoEncoding.Mp4()),
                new TransformationVersionId(UUID.randomUUID())));

        assertThatThrownBy(() -> useCase.start(request("cover", "jpg", null, null, List.of("clip"), null),
                new ByteArrayInputStream(new byte[3]), 3, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(uploadRegistry, assetStore, objectStore, imageTransformer);
    }

    private static StartImageUpload.Request request(
            String uploadKey,
            String extension,
            String desiredContentType,
            Long desiredBytes,
            List<String> eagerTransformationNames,
            String incomingTransformationName
    ) {
        return new StartImageUpload.Request(
                uploadKey, extension, desiredContentType, desiredBytes,
                eagerTransformationNames, incomingTransformationName, null);
    }
}