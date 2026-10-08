package com.example.uploads_service.controller;

import com.example.object_storage.global.GlobalObjectRepository;
import com.example.object_storage.regional.args.PutLocalObjectArgs;
import com.example.uploads_service.api.dto.ImageUploadRequest;
import com.example.uploads_service.api.transformations.NamedImageTransformation;
import com.example.uploads_service.api.uploads.*;
import com.example.uploads_service.named_transformation_repository.NamedTransformationRepository;
import com.example.uploads_service.processing.ImageProcessingApi;
import com.example.uploads_service.processing.ImageProcessingTask;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
@NullMarked
public class ClientController {
    private final GlobalObjectRepository globalObjectRepository;
    private final OriginalUploadRepository uploadRepository;
    private final NamedTransformationRepository namedTransformationRepository;
    private final ImageProcessingApi imageProcessingApi;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    void upload(@RequestPart("file") MultipartFile file, @RequestPart("metadata") @Validated ImageUploadRequest uploadRequest) {

        var fileType = FileType.getByContentType(uploadRequest.getContentType());
        var uploadType = UploadType.IMAGE;
        var extension = fileType == null ? null : fileType.getExtension();
        var registeredContentType = fileType == null ? "application/octet-stream" : fileType.getContentType();
        var region = globalObjectRepository.getHomeRegion();

        // TODO process error
        // check if the constraints pass
        if (uploadRequest.getBytes() != null && file.getSize() != uploadRequest.getBytes())
            throw new RuntimeException("The file size does not match the excepted value.");
        if (uploadRequest.getContentType() != null && !uploadRequest.getContentType().equals(file.getContentType()))
            throw new RuntimeException("The file content type does not match the excepted value.");

        // get the named transformations
        var requestedTransformations = getTransformations(
                uploadRequest.getEagerTransformations(),
                uploadRequest.getIncomingTransformations()
        );

        // register the pending upload
        var uploadToInsert = InsertUpload.builder()
                .uploadKey(uploadRequest.getUploadKey())
                .homeRegion(region)
                .extension(extension)
                .contentType(registeredContentType)
                .bytes(file.getSize())
                .status(UploadStatus.UPLOADING)
                .build();

        var upload = uploadRepository.insert(uploadToInsert);

        // upload to the object storage
        var bucket = "uploads";
        var key = "images/" + upload.getUploadKey() + "/original." + upload.getExtension();
        try {
            globalObjectRepository.putObject(
                    region,
                    PutLocalObjectArgs.builder()
                            .inputStream(file.getInputStream())
                            .contentLength(upload.getBytes())
                            .contentType(upload.getContentType())
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to use the uploaded input stream", e);
        }

        // process or queue the requested transformations
        var task = ImageProcessingTask.builder()
                .upload(upload)
                .eager(requestedTransformations.eager())
                .incoming(requestedTransformations.incoming())
                .completionUrl(uploadRequest.getCompletionUrl())
                .progressUrl(uploadRequest.getProgressUrl())
                .build();

        if (uploadRequest.isAsyncEager())
            imageProcessingApi.processAsync(task);
        else
            imageProcessingApi.processBlocking(task);
    }

    private record NamedTransformationGroups(
            @Nullable List<NamedImageTransformation> eager,
            @Nullable List<NamedImageTransformation> incoming
    ) {
    }

    /**
     * Get the eager and incoming transformation configurations based on their names.
     */
    private NamedTransformationGroups getTransformations(@Nullable Collection<String> eagerNames, @Nullable Collection<String> incomingNames) {
        if (eagerNames == null && incomingNames == null) return new NamedTransformationGroups(null, null);

        var allNames = new HashSet<String>();
        if (eagerNames != null)
            allNames.addAll(eagerNames);
        if (incomingNames != null)
            allNames.addAll(incomingNames);

        if (allNames.isEmpty()) return new NamedTransformationGroups(null, null);

        var allTransformations = namedTransformationRepository.findByName(allNames);

        var eagerTransformations = eagerNames == null ? null : allTransformations.stream().filter(t -> eagerNames.contains(t.getName())).toList();
        var incomingTransformations = incomingNames == null ? null : allTransformations.stream().filter(t -> incomingNames.contains(t.getName())).toList();

        return new NamedTransformationGroups(eagerTransformations, incomingTransformations);
    }
}
