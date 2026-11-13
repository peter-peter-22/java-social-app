package com.example.image_transformer.task_service;

import com.example.image_transformer.transformation.ImageTransformationService;
import com.example.upload_registry.stored_asset_service.CreateStoredAsset;
import com.example.upload_registry.stored_asset_service.StoredAssetService;
import com.example.uploads_api.dto.ImageProcessingTask;
import com.example.uploads_api.stored_asset.StoredAsset;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.named_transformation_repository.NamedImageTransformation;
import com.example.uploads_api.transformations.named_transformation_repository.NamedTransformationRepository;
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

    public void processTask(ImageProcessingTask task) {
        log.info("Processing image task {}", task);

        // get the transformations
        var transformations = getTransformations(task.getEagerTransformationNames(), task.getIncomingTransformationName());


        // apply the transformations
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

                    // apply the incoming transformation
                    Future<StoredAsset> incomingTransformationFuture = null;
                    if (transformations.incoming() != null) {
                        if (transformations.incoming().getOperations().encoding().isDirectory())
                            throw new RuntimeException("""
                                    The encoding of the incoming %s transformation is registered to output multiple files.
                                    It must output a single file.""".formatted(transformations.incoming().getName()));

                        incomingTransformationFuture=executor.submit(() -> {
                            log.debug("Applying transformation: {}", transformations.incoming());
                            var incomingTransformationDir = workDir.resolve("incoming");
                            var asset = processAndSaveTransformation(incomingTransformationDir, originalFilePath, transformations.incoming().getOperations());
                            log.debug("Completed incoming transformations: {}", transformations.incoming().getName());
                            return asset;
                        });
                    }

                    // apply the eager transformations
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

                    var incomingAsset=incomingTransformationFuture.get();
                    var eagerAssets=eagerTransformationFutures.stream().map(future->future.get())
                            // catch, check null, continue finalize
                }


                // finalize the assets and publish the upload
                // TODO set each status to finalizing
                storedAssetService.tagAllAssets();
                // todo check ownership
                // TODO set each status to finalized
                // TODO save final upload registration
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
        var size = FileUtils.getSizeSum(paths);

        // TODO add paths to the stored assets
        // TODO add stored asset status
        var asset = storedAssetService.createStoredAsset(
                CreateStoredAsset.builder()
                        .directory(encoding.isDirectory())
                        .entryPoint(savedDirectory.relativize(entryPoint).toString())
                        .mimeType(encoding.getMimeType())
                        .extension(FileUtils.getFileExtension(entryPoint.getFileName().toString()))
                        .bytes(size)
                        .build()
        );

        // upload the files
        storedAssetService.setTemporaryFiles(asset, paths);
        // TODO set to uploaded if using status?

        return asset;
    }

    private record Transformations(
            @Nullable Collection<NamedImageTransformation> eager,
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