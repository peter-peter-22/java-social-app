package com.example.image_transformer.task_service;

import com.example.image_transformer.transformation.ImageTransformationService;
import com.example.upload_registry.stored_asset_service.CreateStoredAsset;
import com.example.upload_registry.stored_asset_service.StoredAssetService;
import com.example.uploads_api.dto.ImageProcessingTask;
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

        // complete the transformations
        if (transformations != null) {
            Path workDir = null;
            try {
                // create the working directory
                // TODO ensure that the ubuntu tmpfs `/tmp` folder is used and the directory is in the memory
                workDir = Files.createTempDirectory(Path.of("/uploads"), null);

                // download the original upload
                var originalFilePath = workDir.resolve("original." + task.getOriginalAsset().getExtension());
                storedAssetService.downloadMainFile(task.getOriginalAsset(), originalFilePath);

                var transformationsDir = workDir.resolve("transformations");

Path processedFilePath;
                // apply the incoming transformation
                if (transformations.incoming() != null) {
                    log.info("Applying incoming transformation");
                    var incomingTransformationDir = transformationsDir.resolve("incoming");
                    // TODO do th incoming transformation
                }
                else processedFilePath=originalFilePath;

                // apply the eager transformations and upload the results
                if (transformations.eager() != null) {
                    log.info("Applying eager transformations");
                    // TODO replace with executors service
                    for (var transformation : transformations.eager()) {

                        // create the transformed files
                        var transformationInstanceDir = transformationsDir.resolve(transformation.getName());
                        Files.createDirectories(transformationInstanceDir);
                       processAndSaveTransformation(transformationsDir,processedFilePath,transformation.getOperations());
                        // TODO delete transformation instance dir
                    }
                }

                // finalize the assets and publish the upload
                // TODO set each status to finalized
                storedAssetService.tagAllAssets();

            } catch (Exception e) {
                throw new RuntimeException("Error while transforming image", e);
            } finally {
                try {
                    if (workDir != null)
                        FileUtils.deleteRecursively(workDir);
                } catch (IOException e) {
                    log.warn("Error while deleting temporary directory", e);
                }
            }
        }



    }

    private void processAndSaveTransformation(Path workDir, Path inputFile, ImageTransformationOperations operations) throws IOException {
        // TODO add the directory lifecycle here

        // create the transformed files
        var entryPoint = imageTransformationService.transformFile(
                inputFile.toString(),
                workDir,
                operations
        );

        // register the results
        saveTransformationResultsAsStoredAsset(entryPoint, inputFile, operations);
    }

    private void saveTransformationResultsAsStoredAsset(Path savedDirectory, Path entryPoint, ImageTransformationOperations operations) throws IOException {

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
