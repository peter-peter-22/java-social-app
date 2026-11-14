package com.example.image_transformer.task_service;

import com.example.image_transformer.transformation.ImageTransformationService;
import com.example.upload_registry.stored_asset_service.CreateStoredAsset;
import com.example.upload_registry.stored_asset_service.StoredAssetService;
import com.example.upload_registry.upload_service.UploadService;
import com.example.uploads_api.dto.ImageProcessingTask;
import com.example.uploads_api.stored_asset.StoredAsset;
import com.example.uploads_api.stored_asset.StoredAssetStatus;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.named_transformation_repository.NamedImageTransformation;
import com.example.uploads_api.transformations.named_transformation_repository.NamedTransformationRepository;
import com.example.uploads_api.uploads.upload_process.UploadProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
@NullMarked
public class TaskService {
    private final ImageTransformationService imageTransformationService;
    private final StoredAssetService storedAssetService;
    private final NamedTransformationRepository namedTransformationRepository;
    private final UploadService uploadService;

    public void processTask(ImageProcessingTask task) {
        log.info("Processing image task {}", task);

        // get the transformations
        var transformations = getTransformations(task.getEagerTransformationNames(), task.getIncomingTransformationName());

        // apply the transformations
        StoredAsset incomingAsset = null;
        ArrayList<StoredAsset> eagerAssets = null;
        if (transformations != null) {
            AtomicReference<@Nullable Path> workDirRef = new AtomicReference<>();
            try {
                // create the working directory
                // TODO ensure that the ubuntu tmpfs `/tmp` folder is used and the directory is in the memory
                // TODO what happens when the tmp folder exceeds the memory size
                var workDir = Files.createTempDirectory(Path.of("/uploads"), null);
                workDirRef.set(workDir);
                var transformationsDir = workDir.resolve("transformations");

                // download the original upload
                var originalFilePath = workDir.resolve("original." + task.getOriginalAsset().getExtension());
                storedAssetService.downloadMainFile(task.getOriginalAsset(), originalFilePath);

                try (var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {

                    // create the futures of the incoming transformation
                    Future<StoredAsset> incomingTransformationFuture = null;
                    if (transformations.incoming() != null) {
                        if (transformations.incoming().getOperations().encoding().isDirectory())
                            throw new RuntimeException("""
                                    The encoding of the incoming %s transformation is registered to output multiple files.
                                    It must output a single file.""".formatted(transformations.incoming().getName()));

                        incomingTransformationFuture = executor.submit(() -> {
                            log.debug("Applying transformation: {}", transformations.incoming());
                            var incomingTransformationDir = workDir.resolve("incoming");
                            var asset = processAndSaveTransformation(incomingTransformationDir, originalFilePath, transformations.incoming().getOperations());
                            log.debug("Completed incoming transformations: {}", transformations.incoming().getName());
                            return asset;
                        });
                    }

                    // create the futures of the eager transformations
                    var eagerTransformationFutures = new ArrayList<Future<StoredAsset>>();
                    if (transformations.eager() != null) {
                        for (var transformation : transformations.eager()) {
                            eagerTransformationFutures.add(
                                    executor.submit(() -> {
                                        log.debug("Applying eager transformations: {}", transformation);
                                        var transformationInstanceDir = transformationsDir.resolve(transformation.getName());
                                        var asset = processAndSaveTransformation(transformationInstanceDir, originalFilePath, transformation.getOperations());
                                        log.debug("Completed eager transformations: {}", transformation.getName());
                                        return asset;
                                    })
                            );
                        }
                    }

                    // await the futures
                    try {
                        incomingAsset = incomingTransformationFuture == null ? null : incomingTransformationFuture.get();

                        eagerAssets = new ArrayList<StoredAsset>();
                        for (Future<StoredAsset> future : eagerTransformationFutures) {
                            StoredAsset storedAsset = future.get();
                            eagerAssets.add(storedAsset);
                        }
                    } catch (Exception e) {
                        executor.shutdownNow();
                        throw new RuntimeException("Error while awaiting transformation futures: ", e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while transforming image", e);
            } finally {
                try {
                    var workDir = workDirRef.get();
                    if (workDir != null)
                        FileUtils.deleteRecursively(workDir);
                } catch (IOException e) {
                    log.warn("Error while deleting temporary directory", e);
                }
            }
        }

        // finalize the assets (remove the expiration from the objects)
        var allAssets = new ArrayList<StoredAsset>();
        if (eagerAssets != null)
            allAssets.addAll(eagerAssets);
        // if the incoming asset exists, it's kept instead of the original
        allAssets.add(Objects.requireNonNullElseGet(incomingAsset, task::getOriginalAsset));
        var allAssetIds = allAssets.stream().map(StoredAsset::getId).toList();
        storedAssetService.setAssetStatus(allAssetIds, StoredAssetStatus.FINALIZING);
        // removing the tags to remove the expiration
        storedAssetService.tagAllAssets(allAssets, null);
        // check if all objects still exists
        if (!storedAssetService.objectsExist(allAssets))
            // if some of the assets are expired or deleted, this error will be thrown
            throw new RuntimeException("Some of the created assets were not found.");

        // finish the upload
        uploadService.finalizeAttempt(
                task.getAttemptId(),
                task.getUploadKey(),
                buildVariants(transformations, eagerAssets),
                incomingAsset != null ? incomingAsset.getId() : task.getOriginalAsset().getId()
        );
    }

    /**
     * Link the transformations to their assets, build the required data for registering the variants.
     */
    private static @Nullable List<UploadProcessRepository.RegisterVariantArgs> buildVariants(@Nullable Transformations transformations, @Nullable ArrayList<StoredAsset> eagerAssets) {
        // TODO check if the assets are returned in the same order by the executor service as the transformations array
        List<UploadProcessRepository.RegisterVariantArgs> variants = null;
        if (transformations != null && transformations.eager() != null) {
            if (eagerAssets == null)
                throw new IllegalArgumentException("eagerAssets cannot be null");
            if (eagerAssets.size() != transformations.eager().size())
                throw new IllegalStateException("Number of eagerAssets and eager transformations do not match");
            variants = new ArrayList<>();
            for (int i = 0; i < transformations.eager().size(); i++) {
                var transformation = transformations.eager().get(i);
                var asset = eagerAssets.get(i);
                variants.add(
                        new UploadProcessRepository.RegisterVariantArgs(transformation, asset.getId())
                );
            }
        }
        return variants;
    }

    /**
     * Create a temporary directory for the transformation, create the transformed files, register a stored asset, upload the files as temporary objects.
     */
    private StoredAsset processAndSaveTransformation(Path workDir, Path inputFile, ImageTransformationOperations operations) {
        try {
            Files.createDirectories(workDir);

            // create the transformed files
            var entryPoint = imageTransformationService.transformFile(
                    inputFile.toString(),
                    workDir,
                    operations
            );

            // register the results
            return saveTransformationResultsAsStoredAsset(entryPoint, inputFile, operations);
        } catch (IOException e) {
            throw new RuntimeException("Error while processing transformation", e);
        } finally {
            try {
                FileUtils.deleteRecursively(workDir);
            } catch (IOException e) {
                log.warn("Error while deleting temporary directory", e);
            }
        }
    }

    /**
     * Take the contents of a directory and the entrypoint, register a stored asset, upload the files to its object keys.
     * In short, create a stored asset from a directory.
     */
    private StoredAsset saveTransformationResultsAsStoredAsset(Path savedDirectory, Path entryPoint, ImageTransformationOperations operations) throws IOException {
        // register the results
        var encoding = operations.encoding();
        var paths = FileUtils.getFilesInDir(savedDirectory);
        var relativePaths = paths.stream().map(path -> savedDirectory.relativize(path).toString()).toList();
        var size = FileUtils.getSizeSum(paths);

        var asset = storedAssetService.createStoredAsset(
                CreateStoredAsset.builder()
                        .directory(encoding.isDirectory())
                        .entryPoint(savedDirectory.relativize(entryPoint).toString())
                        .objects(relativePaths)
                        .mimeType(encoding.getMimeType())
                        .extension(FileUtils.getFileExtension(entryPoint.getFileName().toString()))
                        .bytes(size)
                        .build()
        );

        // upload the files
        storedAssetService.setTemporaryFiles(asset, paths);

        return asset;
    }

    private record Transformations(
            @Nullable List<NamedImageTransformation> eager,
            @Nullable NamedImageTransformation incoming
    ) {
    }

    /**
     * Get the eager and incoming transformations.
     */
    private @Nullable Transformations getTransformations(@Nullable Collection<String> eagerNames, @Nullable String incomingName) {
        if ((eagerNames == null || eagerNames.isEmpty()) && incomingName == null) return null;

        List<String> allNames = new ArrayList<>();
        if (eagerNames != null)
            allNames.addAll(eagerNames);
        if (incomingName != null)
            allNames.add(incomingName);

        var allTransformations = namedTransformationRepository.getAll(allNames);

        return new Transformations(
                eagerNames == null ? null : allTransformations.stream().filter(t -> allNames.contains(t.getName())).toList(),
                incomingName == null ? null : allTransformations.stream().filter(t -> t.getName().equals(incomingName)).findFirst().orElse(null)
        );
    }
}