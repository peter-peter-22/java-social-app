package com.example.uploads_service_4.upload_service_2;

import com.example.object_storage.global.GlobalObjectRepository;
import com.example.object_storage.global.PutGlobalObjectArgs;
import com.example.uploads_api.v2.transformations.tasks.ImageTask;
import com.example.uploads_api.v2.uploads.upload_registry.*;
import com.example.uploads_service_4.transformation_service.ImageWorkerApi;
import com.example.uploads_service_4.transformation_service.NamedImageTransformationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
public class ImageUploadService {
    private final GlobalObjectRepository globalObjectRepository;
    private final OriginalUploadRepository uploadRepository;
    private final NamedImageTransformationRepository namedImageTransformationRepository;
    private final ImageWorkerApi imageWorkerApi;

    private static final String originalsBucket = "originals";
    private static final String variantsBucket = "variants";

    public void uploadFile(ImageUploadRequest uploadRequest, MultipartFile file) {

        // check if the constraints pass
        if (file.getSize() != uploadRequest.bytes())
            throw new RuntimeException("The file size does not match the excepted value."); // TODO process error

        // insert upload entity
        var fileType = FileType.getByContentType(uploadRequest.contentType());
        var mediaType = MediaType.IMAGE;
        var extension = fileType == null ? null : fileType.getExtension();

        var uploadToInsert = InsertUpload.builder()
                .uploadKey(uploadRequest.uploadKey())
                .homeRegion(globalObjectRepository.getHomeRegion())
                .objectKey(ObjectKeyFactory.getOriginalKey(uploadRequest.uploadKey(), extension))
                .objectBucket(originalsBucket)
                .extension(extension)
                .contentType(uploadRequest.contentType())
                .mediaType(mediaType)
                .bytes(file.getSize())
                .status(UploadStatus.UPLOADING)
                .build();

        var upload = uploadRepository.insert(uploadToInsert);

        // upload file to object storage
        try {
            globalObjectRepository.putObject(
                    PutGlobalObjectArgs.builder()
                            .inputStream(file.getInputStream())
                            .contentLength(upload.getBytes())
                            .contentType(upload.getContentType())
                            .bucket(upload.getObjectBucket())
                            .key(upload.getObjectKey())
                            .build()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to use the uploaded input stream", e);
        }

        // send task to the media processing worker
        var transformations = namedImageTransformationRepository.getByName(uploadRequest.eagerTransformations()).stream()
                .map(
                        namedTransformation -> new ImageTask.ImageTransformationInstance(
                                namedTransformation.name(),
                                namedTransformation.operations()
                        )
                )
                .toList();

        var task = ImageTask.builder()
                .original(upload)
                .completedNotificationUrl(uploadRequest.completionUrl())
                .progressNotificationUrl(uploadRequest.progressUrl())
                .tasks(transformations)
                .build();

        if (uploadRequest.asyncEager()) {
            imageWorkerApi.processAsync(task);

        } else {
            imageWorkerApi.processBlocking(task);
        }
    }

}
