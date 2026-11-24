package com.example.media.api.upload;

import com.example.media.contract.asset.AssetDeliveryProtocol;
import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.asset.NewAsset;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.port.*;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.transformation.NamedTransformation;
import com.example.media.contract.upload.*;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.contract.worker.PlannedImageOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The start-image-upload use case: validate the request, start an attempt, write the original to
 * the object store, and hand the image transform to a worker.
 *
 * <p>The blocking worker adapter retries recoverable failures. Each worker execution allocates fresh
 * result asset ids, so a retry writes to new object keys. If the worker finally fails, the attempt
 * is marked {@code FAILED}; the temporary objects of the failed executions expire.</p>
 *
 * <p>The async adapter publishes the same command and returns immediately. The attempt stays
 * {@code PENDING} until the worker calls the completion endpoint, which finalizes it; a dead-letter
 * marks it {@code FAILED}. The result of an async start is a queued upload, not a finalized one.</p>
 *
 * <p>The order of the command's outputs is the wire contract with the worker: the incoming result
 * first (when requested), then the eager results in request order. The worker reports its results
 * in the same order, and {@link CompleteImageUpload} rebuilds the plan from the attempt row the
 * same way.</p>
 *
 * <p>A rejection is an {@link IllegalArgumentException} (or a
 * {@link com.example.media.contract.transformation.TransformationNotFoundException}) and is
 * delivered as 400; anything else is an unexpected failure.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StartImageUpload {
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final UploadRegistry uploadRegistry;
    private final AssetStore assetStore;
    private final TransformationCatalog transformationCatalog;
    private final ObjectStore objectStore;
    private final ImageTransformer imageTransformer;
    private final CompleteImageUpload completeImageUpload;
    private final FailImageUpload failImageUpload;
    private final TransactionTemplate transactions;

    /**
     * Starts the upload of one image and returns how it was accepted.
     *
     * @param request     the client's request, validated here
     * @param content     the uploaded file
     * @param bytes       the size of the uploaded file
     * @param contentType the content type the client sent the file with, if any
     */
    public StartResult start(Request request, InputStream content, long bytes, @Nullable String contentType) {
        var key = validatedKey(request);
        var extension = validatedExtension(request);
        var eagerNames = validatedEagerNames(request);
        var incomingName = validatedIncomingName(request);
        if (incomingName != null && eagerNames.contains(incomingName))
            throw new IllegalArgumentException(
                    "the incoming transformation must not be repeated as an eager transformation: " + incomingName);
        if (bytes <= 0)
            throw new IllegalArgumentException("the uploaded file is empty");
        if (request.desiredBytes() != null && request.desiredBytes() != bytes)
            throw new IllegalArgumentException("the file size does not match the expected value");
        var resolvedContentType = contentType == null ? DEFAULT_CONTENT_TYPE : contentType;
        if (request.desiredContentType() != null && !request.desiredContentType().equals(resolvedContentType))
            throw new IllegalArgumentException("the file content type does not match the expected value");

        // Resolving is part of the validation: an unknown name or a non-image spec rejects the
        // request before any row or object is written.
        var incoming = incomingName == null ? null : resolveImageTransformation(incomingName);
        var eager = eagerNames.stream().map(this::resolveImageTransformation).toList();

        var placement = objectStore.homePlacement();
        var originalId = AssetId.generate();
        var entryPoint = "original." + extension;

        // One transaction keeps the process lock over the attempt registration.
        var attemptId = transactions.execute(status -> {
            uploadRegistry.openOrGetProcess(key);
            assetStore.registerAsset(new NewAsset(
                    originalId,
                    placement,
                    AssetDeliveryProtocol.IMAGE,
                    resolvedContentType,
                    entryPoint,
                    extension,
                    false,
                    bytes,
                    List.of(entryPoint)));
            var id = uploadRegistry.createAttempt(
                    new NewUploadAttempt(key, originalId, eagerNames, incomingName));
            uploadRegistry.activateAttempt(key, id);
            return id;
        });

        // The original starts temporary: an upload that never finalizes expires on its own.
        var source = placement.prefixOf(originalId).resolve(entryPoint);
        objectStore.put(source, content, bytes, resolvedContentType, ObjectLifetime.TEMPORARY);

        var outputs = new ArrayList<PlannedImageOutput>();
        if (incoming != null)
            outputs.add(plannedOutput(placement, null, incoming));
        for (var transformation : eager)
            outputs.add(plannedOutput(placement, transformation.name(), transformation));

        // A request without any transformation keeps the uploaded original as-is.
        if (outputs.isEmpty())
            return StartResult.blocking(
                    key, completeImageUpload.complete(attemptId, new ImageTransformResult(attemptId, List.of(), null)));

        var command = new ImageTransformCommand(attemptId, source, outputs);
        if (Boolean.TRUE.equals(request.async())) {
            try {
                imageTransformer.transformAsync(command);
            } catch (RuntimeException e) {
                failAttempt(attemptId);
                throw e;
            }
            // The attempt stays PENDING; the completion endpoint finalizes it when the worker calls
            // back. The acceptance time is reported so the client knows when the upload was taken.
            var attempt = uploadRegistry.findAttempt(attemptId)
                    .orElseThrow(() -> new IllegalStateException(
                            "the attempt " + attemptId.value() + " is missing"));
            return StartResult.queued(key, attempt.createdAt());
        }

        ImageTransformResult result;
        try {
            result = imageTransformer.transform(command);
        } catch (RuntimeException e) {
            failAttempt(attemptId);
            throw e;
        }
        return StartResult.blocking(key, completeImageUpload.complete(attemptId, result));
    }

    /**
     * Marks the attempt failed when the worker finally gave up. The blocking adapter retries the
     * recoverable failures internally; the objects of its failed executions stay temporary and
     * expire because their asset identities were never registered.
     */
    private void failAttempt(AttemptId attemptId) {
        try {
            failImageUpload.fail(attemptId);
        } catch (RuntimeException cleanupFailure) {
            // Keep the original worker failure as the primary error.
            log.warn("Could not mark upload attempt {} as failed", attemptId.value(), cleanupFailure);
        }
    }

    /**
     * How an upload was accepted: a finalized upload, or a queued upload whose eager and incoming
     * transformations still run.
     */
    public record StartResult(UploadKey key, @Nullable Upload upload, @Nullable Instant acceptedAt) {

        static StartResult blocking(UploadKey key, Upload upload) {
            return new StartResult(key, upload, null);
        }

        static StartResult queued(UploadKey key, Instant acceptedAt) {
            return new StartResult(key, null, acceptedAt);
        }

        /** Whether the transformations run on the queue; the upload does not exist yet. */
        public boolean async() {
            return upload == null;
        }
    }

    /** The client's image upload request as this use case takes it. Nulls and mismatches are 400s. */
    public record Request(
            @Nullable String uploadKey,
            @Nullable String extension,
            @Nullable String desiredContentType,
            @Nullable Long desiredBytes,
            @Nullable List<String> eagerTransformationNames,
            @Nullable String incomingTransformationName,
            @Nullable Boolean async
    ) {
    }

    private NamedTransformation resolveImageTransformation(String name) {
        var transformation = transformationCatalog.resolve(name);
        if (!(transformation.spec() instanceof ImageTransformationSpec))
            throw new IllegalArgumentException("the transformation is not an image transformation: " + name);
        return transformation;
    }

    /** Plans one worker output under the placement. The worker allocates the final asset id. */
    private static PlannedImageOutput plannedOutput(
            AssetPlacement placement,
            @Nullable String transformationName,
            NamedTransformation transformation
    ) {
        return new PlannedImageOutput(
                transformationName,
                placement,
                (ImageTransformationSpec) transformation.spec());
    }

    private static UploadKey validatedKey(Request request) {
        if (request.uploadKey() == null)
            throw new IllegalArgumentException("the upload key is required");
        return new UploadKey(request.uploadKey());
    }

    private static String validatedExtension(Request request) {
        if (request.extension() == null || request.extension().isBlank())
            throw new IllegalArgumentException("the extension of an image upload is required");
        return request.extension();
    }

    private static List<String> validatedEagerNames(Request request) {
        List<String> names = request.eagerTransformationNames() == null
                ? List.of()
                : request.eagerTransformationNames();
        if (names.stream().anyMatch(name -> name == null || name.isBlank()))
            throw new IllegalArgumentException("the eager transformation names must not be blank");
        if (names.size() != new HashSet<>(names).size())
            throw new IllegalArgumentException("the eager transformation names must be unique");
        return List.copyOf(names);
    }

    private static @Nullable String validatedIncomingName(Request request) {
        var name = request.incomingTransformationName();
        if (name == null)
            return null;
        if (name.isBlank())
            throw new IllegalArgumentException("the incoming transformation name must not be blank");
        return name;
    }
}