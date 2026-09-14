package com.example.image_transformer.task_service;

import com.example.image_transformer.operations.ImageTransformationService;
import com.example.object_storage.global.DownloadGlobalObjectArgs;
import com.example.object_storage.global.GlobalObjectRepository;
import com.example.object_storage.global.UploadGlobalObjectArgs;
import com.example.transformer_contracts.persistence.FileUtils;
import com.example.transformer_contracts.webhook.WebhookService;
import com.example.uploads_api.v2.transformations.tasks.ImageTask;
import com.example.uploads_api.v2.uploads.upload_registry.FileType;
import com.example.uploads_api.v2.uploads.upload_registry.InsertUploadVariant;
import com.example.uploads_api.v2.uploads.upload_registry.UploadVariantRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final WebhookService webhookService;
    private final ImageTransformationService transformationService;
    private final GlobalObjectRepository globalObjectRepository;
    private final UploadVariantRepository uploadVariantRepository;

    public void processTask(@NonNull ImageTask task) {
        // define download path and task directory with path manager
        // download original to disk
        // process task
        // upload all outputs, use variant key manager

        // requirements: create path manage in transformer contracts and object key manage in upload contracts or upload persistence


        // define the used paths
        var original = task.getOriginal();

        Path taskDir = Path.of("1");
        Path inputFile = taskDir.resolve("input." + original.getExtension());
        Path outputDir = taskDir.resolve("output");

        // download the original to disk
        var downloadArgs = DownloadGlobalObjectArgs.builder()
                .bucket(original.getObjectBucket())
                .key(original.getObjectKey())
                .homeRegion(original.getHomeRegion())
                .destination(inputFile)
                .build();
        globalObjectRepository.download(downloadArgs);

        // process the tasks
        for (var variant : task.getVariants()) {
            Path variantDir = taskDir.resolve(variant.name());
            String keyPrefix = original.getUploadKey() + "/" + variant.name();

            var mainFile = transformationService.transformFile(inputFile.toString(), outputDir, variant.operations());

            uploadVariantRepository.insert(
                    InsertUploadVariant.builder()
                            .mainObjectContentType()// no main object and content type. object roles need to be defined
            )

            try {
                var files = FileUtils.getFilesInDir(variantDir);

                var uploadArgs = files.stream()
                        .map(file -> {
                            var fileType = FileType.fromPath(file);
                            var contentType = fileType != null ? fileType.getContentType() : "application/octet-stream";
                            var relativePath = variantDir.relativize(file);
                            UploadGlobalObjectArgs args;
                            args = UploadGlobalObjectArgs.builder()
                                    .source(file)
                                    .contentType(contentType)
                                    .bucket("variants")
                                    .key(keyPrefix + "/" + relativePath)
                                    .build();
                            return args;
                        })
                        .toList();

                globalObjectRepository.uploadAll(uploadArgs);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to read the files in the variant directory '%s'", variantDir), e);
            }
        }
    }
}
