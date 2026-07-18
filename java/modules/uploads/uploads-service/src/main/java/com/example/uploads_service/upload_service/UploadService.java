package com.example.uploads_service.upload_service;

import com.example.object_storage.repository.GetPreSignedUploadFormArgs;
import com.example.object_storage.repository.ObjectStorageRepository;
import com.example.uploads_api.transformations.upload_repository.InsertUpload;
import com.example.uploads_api.transformations.upload_repository.UploadRepository;
import com.example.uploads_api.uploads.UploadId;
import com.example.uploads_service.transformation_service.TransformationService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public class UploadService {
    private final UploadRepository uploadRepository;
    private final ObjectStorageRepository objectStorageRepository;
    private final TransformationService transformationService;
    private final static String UPLOAD_BUCKET = "uploads";

    /**
     * Generate a post form for signed file upload, register the upload in the database, and return the relevant data.
     */
    @NotNull SignedUploadFormResponse createSignedUpload(@NonNull CreateSignedUploadArgs args) {
        // register to database
        var insertUpload = InsertUpload.builder()
                .bucket(UPLOAD_BUCKET)
                .objectPath(args.getObjectPath())
                .fileType(args.getFileType())
                .createdBy(args.getCreatedBy())
                .build();
        var id = uploadRepository.create(insertUpload);

        // get form parameters
        var getFormArgs = GetPreSignedUploadFormArgs.builder()
                .bucket(UPLOAD_BUCKET)
                .objectPath(args.getObjectPath())
                .expiration(args.getExpiration())
                .timeUnit(args.getTimeUnit())
                .contentType(args.getFileType().getContentType())
                .contentLengthRange(new GetPreSignedUploadFormArgs.ContentLengthRange(
                        args.getMinLengthBytes(),
                        args.getMaxLengthBytes()
                ))
                .build();
        var formParams = objectStorageRepository.getPreSignedUploadForm(getFormArgs);

        // get url
        var url = objectStorageRepository.getBucketUrl(UPLOAD_BUCKET);

        return SignedUploadFormResponse.builder()
                .formFields(formParams)
                .uploadId(id)
                .uploadUrl(url)
                .build();
    }

    /**
     * After a file is uploaded to object storage, mark the upload as complete.
     */
    void markUploadAsComplete(@NotNull UploadId id) {

    }

}
