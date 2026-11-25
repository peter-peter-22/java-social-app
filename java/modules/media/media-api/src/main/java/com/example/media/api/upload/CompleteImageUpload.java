package com.example.media.api.upload;

import com.example.media.contract.asset.*;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.transformation.NamedTransformation;
import com.example.media.contract.upload.*;
import com.example.media.contract.variant.NewVariant;
import com.example.media.contract.variant.VariantId;
import com.example.media.contract.worker.CompletedImageOutput;
import com.example.media.contract.worker.ImageTransformResult;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

/**
 * The complete-image-upload use case: finish one attempt from a worker's result.
 *
 * <p>This is the finalize of the upload lifecycle: register the result assets, make the kept
 * objects permanent, and commit one transaction where the assets become ready, the upload and
 * variant rows are inserted, and the attempt becomes successful. If that commit fails, the objects
 * expire and the cleanup rules apply. The blocking upload runs this before it responds; the async
 * completion endpoint runs the same use case.</p>
 *
 * <p>The plan is rebuilt from the attempt row: the transformation names recorded at start are
 * resolved again and the result outputs are matched to them in the command's order, the incoming
 * result first (when requested), then the eager results in the recorded order. The produced
 * variants pin the versions current at completion.</p>
 *
 * <p>The incoming result replaces the uploaded original as the upload's main asset; the replaced
 * original is left out of the finalize so its temporary objects expire.</p>
 */
@Service
@RequiredArgsConstructor
public class CompleteImageUpload {
    private final UploadRegistry uploadRegistry;
    private final AssetStore assetStore;
    private final TransformationCatalog transformationCatalog;
    private final ObjectStore objectStore;
    private final TransactionTemplate transactions;

    /**
     * Finishes the attempt and returns the finalized upload.
     *
     * @throws UploadNotFoundException  when the attempt is unknown (404)
     * @throws IllegalArgumentException when the result is for a different attempt (400)
     */
    public Upload complete(AttemptId attemptId, ImageTransformResult result) {
        var attempt = uploadRegistry.findAttempt(attemptId)
                .orElseThrow(() -> new UploadNotFoundException(
                        "could not find upload attempt: " + attemptId.value()));
        if (!attemptId.equals(result.attemptId()))
            throw new IllegalArgumentException(
                    "the worker result is for a different attempt: " + result.attemptId().value());

        // Async completion may be retried. A successful attempt returns its existing upload and
        // ignores later results; a failed attempt accepts no completion at all.
        if (attempt.status() == UploadAttemptStatus.SUCCESS)
            return uploadRegistry.findUpload(attempt.key())
                    .orElseThrow(() -> new IllegalStateException(
                            "the successful attempt " + attemptId.value() + " has no upload"));
        if (attempt.status() == UploadAttemptStatus.FAILED)
            throw new IllegalStateException(
                    "the attempt " + attemptId.value() + " failed and cannot be completed");

        var eagerNames = attempt.eagerTransformationNames();
        var incomingName = attempt.incomingTransformationName();
        var plannedCount = eagerNames.size() + (incomingName == null ? 0 : 1);
        if (result.outputs().size() != plannedCount)
            throw new IllegalStateException("the worker returned %d outputs for %d planned outputs of attempt %s"
                    .formatted(result.outputs().size(), plannedCount, attemptId.value()));

        // The plan of the command: the incoming result first, then the eager results in order.
        CompletedImageOutput incomingOutput = incomingName == null ? null : result.outputs().getFirst();
        var eagerOutputs = incomingName == null
                ? result.outputs()
                : List.copyOf(result.outputs().subList(1, result.outputs().size()));
        var eagerTransformations = resolveAll(eagerNames);

        var placement = objectStore.homePlacement();

        // The worker allocates fresh result asset ids on every execution. Validate them before
        // anything is registered: each id must be unique and its prefix must be the asset id
        // under the placement the command carried.
        var resultIds = new HashSet<AssetId>();
        for (var output : result.outputs()) {
            if (!resultIds.add(output.assetId()))
                throw new IllegalStateException(
                        "the worker returned the same asset id twice for attempt " + attemptId.value());
            if (!placement.prefixOf(output.assetId()).equals(output.prefix()))
                throw new IllegalStateException(
                        "the worker returned an unexpected prefix for asset " + output.assetId().value());
        }

        // The result assets: the successful execution reports their ids and objects. A retried
        // completion of the same result may already have registered some of them; those rows are
        // still uploading and identical, so they are reused instead of inserted again.
        for (var output : result.outputs())
            registerResultAsset(placement, output);

        if (result.sourceMetadata() != null) {
            var originalAsset = assetStore.findAsset(attempt.originalAssetId())
                    .orElseThrow(() -> new IllegalStateException(
                            "the original asset of attempt " + attemptId.value() + " is missing"));
            if (originalAsset.image() == null)
                assetStore.setImageMetadata(attempt.originalAssetId(), result.sourceMetadata());
        }

        // The kept assets: the incoming result replaces the uploaded original as the main asset.
        var mainAssetId = incomingOutput == null ? attempt.originalAssetId() : incomingOutput.assetId();
        var keptIds = new ArrayList<AssetId>();
        keptIds.add(mainAssetId);
        for (var output : eagerOutputs)
            keptIds.add(output.assetId());
        var keptRefs = new ArrayList<ObjectRef>();
        if (incomingOutput == null) {
            // The uploaded original is the kept main asset; its registered objects are made
            // permanent. Fetching it also proves the row exists before anything is committed.
            var originalAsset = assetStore.findAsset(attempt.originalAssetId())
                    .orElseThrow(() -> new IllegalStateException(
                            "the original asset of attempt " + attemptId.value() + " is missing"));
            for (var object : originalAsset.objects())
                keptRefs.add(originalAsset.prefix().resolve(object));
        }
        for (var output : result.outputs())
            for (var object : output.objects())
                keptRefs.add(output.prefix().resolve(object));

        assetStore.setStatus(keptIds, AssetStatus.FINALIZING);
        // Making the kept objects permanent proves that they still exist: the tag write fails on a
        // missing object, so no rows are committed for objects that expired during the upload.
        objectStore.setLifetime(keptRefs, ObjectLifetime.PERMANENT);

        return transactions.execute(_ -> {
            assetStore.setStatus(keptIds, AssetStatus.READY);
            var upload = uploadRegistry.registerUpload(
                    new NewUpload(attempt.key(), attemptId, mainAssetId, attempt.visibility()));
            var variants = new ArrayList<NewVariant>();
            for (int i = 0; i < eagerOutputs.size(); i++) {
                var transformation = eagerTransformations.get(i);
                variants.add(new NewVariant(
                        new VariantId(upload.id(), transformation.id()),
                        transformation.currentVersionId(),
                        eagerOutputs.get(i).assetId()));
            }
            uploadRegistry.registerVariants(variants);
            uploadRegistry.finishAttempt(attemptId, AttemptOutcome.SUCCESS);
            return upload;
        });
    }

    /** The recorded names of the attempt, resolved in the same order. */
    private List<NamedTransformation> resolveAll(List<String> names) {
        if (names.isEmpty())
            return List.of();
        var byName = new HashMap<String, NamedTransformation>();
        for (var transformation : transformationCatalog.resolveAll(names))
            byName.put(transformation.name(), transformation);
        return names.stream().map(byName::get).toList();
    }

    private static @Nullable String extensionOf(String entryPoint) {
        var dot = entryPoint.lastIndexOf('.');
        return dot < 0 ? null : entryPoint.substring(dot + 1);
    }

    /**
     * Registers one result asset, or reuses an identical row left by an earlier attempt at the same
     * completion. A row with a different shape is an error: assets are immutable and two executions
     * must never share an identity.
     */
    private void registerResultAsset(AssetPlacement placement, CompletedImageOutput output) {
        var existing = assetStore.findAsset(output.assetId()).orElse(null);
        if (existing == null) {
            assetStore.registerAsset(new NewAsset(
                    output.assetId(),
                    placement,
                    AssetDeliveryProtocol.IMAGE,
                    output.contentType(),
                    output.entryPoint(),
                    extensionOf(output.entryPoint()),
                    output.objects().size() > 1,
                    output.bytes(),
                    output.objects()));
            assetStore.setImageMetadata(output.assetId(), output.metadata());
            return;
        }
        if (!matchesUploadingAsset(existing, placement, output))
            throw new IllegalStateException(
                    "the asset " + output.assetId().value() + " already exists with different content");
        if (existing.image() == null)
            assetStore.setImageMetadata(output.assetId(), output.metadata());
    }

    private static boolean matchesUploadingAsset(
            Asset asset, AssetPlacement placement, CompletedImageOutput output) {
        return asset.status() == AssetStatus.UPLOADING
                && asset.placement().equals(placement)
                && asset.deliveryProtocol() == AssetDeliveryProtocol.IMAGE
                && asset.contentType().equals(output.contentType())
                && asset.entryPoint().equals(output.entryPoint())
                && Objects.equals(asset.extension(), extensionOf(output.entryPoint()))
                && asset.directory() == (output.objects().size() > 1)
                && asset.bytes() == output.bytes()
                && asset.objects().equals(output.objects());
    }
}