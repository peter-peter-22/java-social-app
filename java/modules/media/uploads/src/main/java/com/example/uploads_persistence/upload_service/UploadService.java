package com.example.uploads_persistence.upload_service;

import com.example.object_storage.repository.ObjectStorageRepository;
import com.example.uploads_persistence.transformations.TransformationService;
import com.example.uploads_persistence.upload_repository.InsertUpload;
import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.UploadId;
import com.example.media_api.uploads.UploadStatus;
import com.example.users_api.repository.UserId;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import com.example.uploads_persistence.upload_repository.UploadRepository;

import java.util.Map;

@RequiredArgsConstructor
public class UploadService {
    private final UploadRepository uploadRepository;
    private final ObjectStorageRepository objectStorageRepository;
    private final TransformationService transformationService;
    private final static String UPLOAD_BUCKET="uploads";

    @NotNull Map<String, String> createSignedUpload(@NotNull String objectPath, @NotNull FileType fileType, @NotNull UserId createdBy){
        var insertUpload = InsertUpload.builder()
                .bucket(UPLOAD_BUCKET)
                .objectPath(objectPath)
                .fileType(fileType)
                .createdBy(createdBy)
                .build();
        uploadRepository.create(insertUpload);
        // not implemented yet
        return Map.of();
    }

    /** After a file is uploaded to object storage, mark the upload as complete
     * @return True if the upload is awaiting lazy transformations, false if ready or the upload is already complete and nothing happened. */
    boolean markUploadAsComplete(@NotNull UploadId id){
        // TODO make retriable
        var updated = uploadRepository.updateStatus(id, UploadStatus.PROCESSING);
        if (updated == null) return false;
        return transformationService.applyTransformations(updated);
    }

}
