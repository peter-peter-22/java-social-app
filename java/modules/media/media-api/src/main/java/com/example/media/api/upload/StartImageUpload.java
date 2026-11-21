package com.example.media.api.upload;

import com.example.media.contract.asset.AssetDeliveryProtocol;
import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.asset.NewAsset;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.object.ObjectPrefix;
import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.ImageTransformer;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.transformation.NamedTransformation;
import com.example.media.contract.upload.NewUploadAttempt;
import com.example.media.contract.upload.Upload;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.contract.worker.PlannedImageOutput;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The start-image-upload use case: validate the request, start an attempt, write the original to
 * the object store, and run the blocking image transform.
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
public class StartImageUpload {
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final UploadRegistry uploadRegistry;
    private final AssetStore assetStore;
    private final TransformationCatalog transformationCatalog;
    private final ObjectStore objectStore;
    private final ImageTransformer imageTransformer;
    private final CompleteImageUpload completeImageUpload;
    private final TransactionTemplate transactions;

    /**
     * Runs the blocking upload of one image and returns the finalized upload.
     *
     * @param request     the client's request, validated here
     * @param content     the uploaded file
     * @param bytes       the size of the uploaded file
     * @param contentType the content type the client sent the file with, if any
     */
    public Upload start(Request request, InputStream content, long bytes, @Nullable String contentType) {
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
        var source = prefixOf(placement, originalId).resolve(entryPoint);
        objectStore.put(source, content, bytes, resolvedContentType, ObjectLifetime.TEMPORARY);

        var outputs = new ArrayList<PlannedImageOutput>();
        if (incoming != null)
            outputs.add(plannedOutput(placement, null, incoming));
        for (var transformation : eager)
            outputs.add(plannedOutput(placement, transformation.name(), transformation));

        // A request without any transformation keeps the uploaded original as-is.
        ImageTransformResult result = outputs.isEmpty()
                ? new ImageTransformResult(attemptId, List.of(), null)
                : imageTransformer.transform(new ImageTransformCommand(attemptId, source, outputs));

        return completeImageUpload.complete(attemptId, result);
    }

    /** The client's image upload request as this use case takes it. Nulls and mismatches are 400s. */
    public record Request(
            @Nullable String uploadKey,
            @Nullable String extension,
            @Nullable String desiredContentType,
            @Nullable Long desiredBytes,
            @Nullable List<String> eagerTransformationNames,
            @Nullable String incomingTransformationName
    ) {
    }

    private NamedTransformation resolveImageTransformation(String name) {
        var transformation = transformationCatalog.resolve(name);
        if (!(transformation.spec() instanceof ImageTransformationSpec))
            throw new IllegalArgumentException("the transformation is not an image transformation: " + name);
        return transformation;
    }

    /** Reserves the output's asset id and its prefix before the worker writes under it. */
    private static PlannedImageOutput plannedOutput(
            AssetPlacement placement,
            @Nullable String transformationName,
            NamedTransformation transformation
    ) {
        var assetId = AssetId.generate();
        return new PlannedImageOutput(
                assetId,
                prefixOf(placement, assetId),
                transformationName,
                (ImageTransformationSpec) transformation.spec());
    }

    /** The asset id is the prefix, so the keys of two assets cannot collide. */
    private static ObjectPrefix prefixOf(AssetPlacement placement, AssetId id) {
        return new ObjectPrefix(placement.homeRegion(), placement.objectBucket(), id.value().toString());
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