package com.example.media.imageworker.transform;

import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.PlannedImageOutput;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * The use case downloads the source, transforms it per planned output, uploads the produced files
 * under the given prefixes as temporary objects, and reports them in the command's order.
 */
class TransformImageTest {

    private final ObjectStore objectStore = mock();
    private final ImageProcessor imageProcessor = mock();
    private final TransformImage useCase = new TransformImage(objectStore, imageProcessor);

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final ObjectRef SOURCE = new ObjectRef("eu-west", "uploads", "3f2b/original_jpg");
    private static final AssetPlacement PLACEMENT = new AssetPlacement("eu-west", "uploads");
    private static final ImageTransformationSpec WEBP_50 =
            new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50));
    private static final ImageTransformationSpec JPEG_80 =
            new ImageTransformationSpec(null, null, null, new ImageEncoding.Jpeg(80));

    private final AtomicReference<Path> outputDir = new AtomicReference<>();

    @Test
    void downloadsTransformsAndUploadsUnderThePlacementWithFreshAssetIds() {
        var incoming = plannedOutput(null, WEBP_50);
        var eager = plannedOutput("profile", JPEG_80);
        var command = new ImageTransformCommand(ATTEMPT_ID, SOURCE, List.of(incoming, eager));
        stubProcessing();
        when(imageProcessor.readMetadata(any())).thenReturn(new ImageMetadata(400, 300));

        var result = useCase.transform(command);

        verify(objectStore).download(eq(SOURCE), any());
        assertThat(result.attemptId()).isEqualTo(ATTEMPT_ID);
        assertThat(result.sourceMetadata()).isEqualTo(new ImageMetadata(400, 300));
        assertThat(result.outputs()).hasSize(2);

        // the outputs are reported in the command's order: the incoming result first
        var incomingResult = result.outputs().getFirst();
        assertThat(incomingResult.assetId()).isNotNull();
        assertThat(incomingResult.prefix()).isEqualTo(PLACEMENT.prefixOf(incomingResult.assetId()));
        assertThat(incomingResult.entryPoint()).isEqualTo("default.webp");
        assertThat(incomingResult.objects()).containsExactly("default.webp");
        assertThat(incomingResult.bytes()).isEqualTo(3);
        assertThat(incomingResult.contentType()).isEqualTo("image/webp");
        assertThat(incomingResult.metadata()).isEqualTo(new ImageMetadata(10, 10));
        verify(objectStore).put(
                eq(incomingResult.prefix().resolve("default.webp")),
                any(Path.class),
                eq("image/webp"),
                eq(ObjectLifetime.TEMPORARY));

        var eagerResult = result.outputs().getLast();
        assertThat(eagerResult.assetId()).isNotNull();
        assertThat(eagerResult.assetId()).isNotEqualTo(incomingResult.assetId());
        assertThat(eagerResult.prefix()).isEqualTo(PLACEMENT.prefixOf(eagerResult.assetId()));
        assertThat(eagerResult.entryPoint()).isEqualTo("default.jpg");
        assertThat(eagerResult.objects()).containsExactly("default.jpg");
        assertThat(eagerResult.bytes()).isEqualTo(3);
        assertThat(eagerResult.contentType()).isEqualTo("image/jpeg");
        verify(objectStore).put(
                eq(eagerResult.prefix().resolve("default.jpg")),
                any(Path.class),
                eq("image/jpeg"),
                eq(ObjectLifetime.TEMPORARY));
    }

    /** The overwrite regression: retrying a command must never reuse an object key. */
    @Test
    void aRetriedCommandGetsFreshObjectKeys() {
        var command = new ImageTransformCommand(ATTEMPT_ID, SOURCE, List.of(plannedOutput(null, WEBP_50)));
        stubProcessing();
        when(imageProcessor.readMetadata(any())).thenReturn(new ImageMetadata(400, 300));

        var first = useCase.transform(command);
        var second = useCase.transform(command);

        assertThat(first.outputs().getFirst().assetId())
                .isNotEqualTo(second.outputs().getFirst().assetId());
        assertThat(first.outputs().getFirst().prefix())
                .isNotEqualTo(second.outputs().getFirst().prefix());

        var refs = ArgumentCaptor.forClass(ObjectRef.class);
        verify(objectStore, times(2)).put(
                refs.capture(), any(Path.class), eq("image/webp"), eq(ObjectLifetime.TEMPORARY));
        assertThat(refs.getAllValues()).doesNotHaveDuplicates();
    }

    @Test
    void deletesTheWorkDirectoryWhenTheRunEnds() {
        var command = new ImageTransformCommand(ATTEMPT_ID, SOURCE, List.of(plannedOutput(null, WEBP_50)));
        stubProcessing();

        useCase.transform(command);

        assertThat(outputDir.get()).isNotNull();
        assertThat(Files.exists(outputDir.get().getParent())).isFalse();
    }

    @Test
    void aFailedTransformUploadsNothingAndCleansUp() {
        var command = new ImageTransformCommand(ATTEMPT_ID, SOURCE, List.of(plannedOutput(null, WEBP_50)));
        when(imageProcessor.process(any(), any(), any())).thenAnswer(invocation -> {
            outputDir.set(invocation.getArgument(1, Path.class));
            throw new IllegalStateException("boom");
        });

        assertThatThrownBy(() -> useCase.transform(command)).isInstanceOf(IllegalStateException.class);

        verify(objectStore, never()).put(any(), any(), any(), any());
        assertThat(Files.exists(outputDir.get().getParent())).isFalse();
    }

    private static PlannedImageOutput plannedOutput(
            String transformationName,
            ImageTransformationSpec spec
    ) {
        return new PlannedImageOutput(transformationName, PLACEMENT, spec);
    }

    /** Stands in for the libvips processing: writes one encoded file into the output directory. */
    private void stubProcessing() {
        when(imageProcessor.process(any(), any(), any())).thenAnswer(invocation -> {
            var dir = invocation.getArgument(1, Path.class);
            outputDir.set(dir);
            var spec = invocation.getArgument(2, ImageTransformationSpec.class);
            var entryPoint = dir.resolve(spec.encoding() instanceof ImageEncoding.Jpeg ? "default.jpg" : "default.webp");
            Files.write(entryPoint, new byte[3]);
            return new ImageProcessor.ProcessedFile(entryPoint, new ImageMetadata(10, 10));
        });
    }
}