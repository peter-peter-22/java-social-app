package com.example.upload_registry.upload_service;

import com.example.upload_registry.stored_asset_service.CreateStoredAsset;
import com.example.upload_registry.stored_asset_service.StoredAssetService;
import com.example.uploads_api.stored_asset.AssetDeliveryProtocol;
import com.example.uploads_api.uploads.Upload;
import com.example.uploads_api.uploads.UploadRepository;
import com.example.uploads_api.uploads.UploadType;
import com.example.uploads_api.uploads.upload_process.InsertUploadAttempt;
import com.example.uploads_api.uploads.upload_process.UploadAlreadyExistsException;
import com.example.uploads_api.uploads.upload_process.UploadProcessRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@NullMarked
@Slf4j
public class UploadService {
    private final UploadProcessRepository uploadProcessRepository;
    private final UploadRepository uploadRepository;
    private final StoredAssetService storedAssetService;

    @Transactional
    public StartedUploadAttempt startUploadProcess(
            @Nullable String extension,
            long bytes,
            String uploadKey,
            @Nullable List<String> eagerTransformationNames,
            @Nullable String incomingTransformationName,
            UploadType uploadType
    ) throws UploadAlreadyExistsException {

        var process = uploadProcessRepository.createProcessIfNotExistsThenLock(uploadKey);

        // The previous attempt always has a file, we must decide if its the same
        var storedAsset = storedAssetService.createStoredAsset(
                CreateStoredAsset.builder()
                        .directory(false)
                        .entryPoint("original")
                        .extension(extension)
                        .bytes(bytes)
                        .deliveryProtocol(AssetDeliveryProtocol.uploadTypeToDeliveryProtocol(uploadType))
                        .build()
        );

        var attemptId = uploadProcessRepository.createAttempt(
                InsertUploadAttempt.builder()
                        .eagerTransformationNames(eagerTransformationNames)
                        .incomingTransformationName(incomingTransformationName)
                        .originalAssetId(storedAsset.getId())
                        .build()
        );
        uploadProcessRepository.setAttemptAsActive(process.getUploadKey(), attemptId);

        log.info("Created upload attempt {} and asset {} for upload process {}.", attemptId, storedAsset.getId(), process.getUploadKey());

        return new StartedUploadAttempt(process.getUploadKey(), attemptId, storedAsset);
    }

    public Upload finalizeAttempt(UUID attemptId, String uploadKey, @Nullable Collection<UploadProcessRepository.RegisterVariantArgs> variants, UUID originalAssetId) {
        return uploadProcessRepository.finalizeAttempt(attemptId, uploadKey, variants, originalAssetId);
    }

    public Upload findUploadByKey(String key) {
        return uploadRepository.findByKey(key);
    }

    public Upload findImageUploadByKey(String key) {
        return uploadRepository.findImageByKey(key);
    }

    public Upload findVideoUploadByKey(String key) {
        return uploadRepository.findVideoByKey(key);
    }
}
