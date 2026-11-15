package com.example.uploads_service.uploads;

import com.example.upload_services.stored_asset_service.StoredAssetService;
import com.example.upload_services.upload_service.UploadService;
import com.example.uploads_api.dto.ImageProcessingTask;
import com.example.uploads_api.dto.ImageUploadRequest;
import com.example.uploads_api.dto.UploadCreatedResponse;
import com.example.uploads_api.uploads.UploadType;
import com.example.uploads_service.processing.MediaWorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@NullMarked
@RequiredArgsConstructor
@Slf4j
public class ImageUploadProcessingService {
    private final UploadService uploadService;
    private final StoredAssetService storedAssetService;
    private final MediaWorkerService mediaWorkerService;

    /**
     * The upload process:
     * Validate the request.
     * Upload the original file to the object storage into a temporary object.
     * Forward the original key and the requested transformations to the media worker.
     * Process the transformations, write the required rows, and finalize.
     * <p>
     * We imply that the input stream is always new and the object storage cleans up the temporal files based on the tag.
     * </p>
     * TODO format this comment properly, separate each step into a new row
     */
    public UploadCreatedResponse processUpload(ImageUploadRequest imageUploadRequest, MultipartFile file) {
        log.info("Starting processing of image upload. Upload request: {}", imageUploadRequest);

        var contentType = file.getContentType();
        var bytes = file.getSize();

        // validate signature
        // maybe database upload keys would be better

        // validate constraints
        if (imageUploadRequest.getDesiredBytes() != null && bytes != imageUploadRequest.getDesiredBytes())
            throw new RuntimeException("The file size does not match the excepted value.");
        if (imageUploadRequest.getDesiredContentType() != null && !imageUploadRequest.getDesiredContentType().equals(contentType))
            throw new RuntimeException("The file content type does not match the excepted value.");

        // register the pending upload
        String defaultContentType = "application/octet-stream";
        var startedAttempt = uploadService.startUploadProcess(
                imageUploadRequest.getExtension(),
                bytes,
                imageUploadRequest.getUploadKey(),
                imageUploadRequest.getEagerTransformations(),
                imageUploadRequest.getIncomingTransformation(),
                UploadType.IMAGE,
                contentType == null ? defaultContentType : contentType
        );

        // upload to the object storage
        try {
            storedAssetService.setTemporaryFile(startedAttempt.originalUpload(), file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error while processing uploading the temporary original file:", e);
        }

        // process or queue the requested transformations
        var task = ImageProcessingTask.builder()
                .originalAsset(startedAttempt.originalUpload())
                .uploadKey(imageUploadRequest.getUploadKey())
                .attemptId(startedAttempt.attemptId())
                .eagerTransformationNames(imageUploadRequest.getEagerTransformations())
                .incomingTransformationName(imageUploadRequest.getIncomingTransformation())
                .build();

        if (imageUploadRequest.isAsync())
            mediaWorkerService.processImageBlocking(task);
        else
            mediaWorkerService.processImageAsync(task);

        return new UploadCreatedResponse(imageUploadRequest.isAsync());
    }
}
