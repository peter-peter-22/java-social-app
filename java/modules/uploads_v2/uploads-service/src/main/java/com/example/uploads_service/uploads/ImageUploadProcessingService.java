package com.example.uploads_service.uploads;

import com.example.upload_registry.stored_asset_service.StoredAssetService;
import com.example.upload_registry.upload_service.UploadService;
import com.example.uploads_api.dto.ImageProcessingTask;
import com.example.uploads_api.dto.ImageUploadRequest;
import com.example.uploads_api.dto.UploadCreatedResponse;
import com.example.uploads_api.transformations.named_transformation_repository.NamedImageTransformation;
import com.example.uploads_api.transformations.named_transformation_repository.NamedTransformationRepository;
import com.example.uploads_api.uploads.UploadType;
import com.example.uploads_service.processing.MediaWorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@NullMarked
@RequiredArgsConstructor
@Slf4j
public class ImageUploadProcessingService {
    private final NamedTransformationRepository namedTransformationRepository;
    private final UploadService uploadService;
    private final StoredAssetService storedAssetService;
    private final MediaWorkerService  mediaWorkerService;

    /**
     * The upload process:
     * Validate the request.
     * Upload the original file to the object storage into a temporary object.
     * Forward the original key and the requested transformations to the media worker.
     * Process the transformations, write the required rows, and finalize.
     * <p>
     * We imply that the input stream is always new and the object storage cleans up the temporal files based on the tag.
     * </p>
     * <p>
     * Significant points of failure:
     * - Before finalization: The temporary files are orphaned and should eventually disappear. The SQL rows of the attempt remain.
     * - During finalization: The (persistent) stored assets with expired finalizing status are periodically cleaned up.
     * - After finalization: The transaction rolls back and removes the finalization status.
     * </p>
     * TODO format this comment properly
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
        var startedAttempt = uploadService.startUploadProcess(
                imageUploadRequest.getExtension(),
                bytes,
                imageUploadRequest.getUploadKey(),
                imageUploadRequest.getEagerTransformations(),
                imageUploadRequest.getIncomingTransformation(),
                UploadType.IMAGE
        );

        // upload to the object storage
        try {
            storedAssetService.setTemporaryFile(startedAttempt.originalUpload(), file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error while processing uploading the temporary original file:", e);
        }

        // process or queue the requested transformations
        var task=ImageProcessingTask.builder();//TODO send message and build the worker
        mediaWorkerService.processImageBlocking();

        return new UploadCreatedResponse(imageUploadRequest.isAsync());
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
