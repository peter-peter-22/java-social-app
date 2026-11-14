package com.example.uploads_api.uploads.upload_process;

import com.example.uploads_api.transformations.named_transformation_repository.NamedImageTransformation;
import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

@NullMarked
public interface UploadProcessRepository {
    /**
     * Create a new upload process for this upload key if it does not exist, do nothing otherwise.
     * Return the upload process in either case.
     * Use pessimistic lock the upload process.
     */
    UploadProcess createProcessIfNotExistsThenLock(String uploadKey);

    /**
     * Insert a new upload attempt.
     */
    UUID createAttempt(InsertUploadAttempt insert);

    /**
     * Get an upload attempt by id. Use pessimistic locking.
     */
    @Nullable UploadAttempt getAndLockAttempt(UUID attemptId);

    /**
     * Set an upload attempt as active in an upload process.
     * Has effect only when the upload process has no upload ID set. (So when the upload is not finished.)
     * Throws error when the process is finished.
     */
    // TODO should throw UploadAlreadyExistsException("Upload with uploadKey %s is already finished, cannot start a new attempt.".formatted(uploadKey));
    void setAttemptAsActive(String uploadKey, UUID attemptId) throws UploadAlreadyExistsException;

    /**
     * Add a new asset to the created assets of an attempt.
     */
    void registerCreatedAsset(UUID attemptId, UUID assetId);

    record RegisterVariantArgs(NamedImageTransformation transformation, UUID storedAssetId){}

    /**
     * Publish the final upload if the selected attempt is still active.
     * Steps:
     * Select and lock the upload process.
     * Check if the current attempt is active in the retrieved process, and if it does not have a completed upload.
     * Create the upload and variant rows.
     * Update the upload process to register the upload.
     * Update the status of the attempt to SUCCESS, set the finish date, also lock it and check if it exists.
     * If all successful, the complete upload is published, otherwise an error is thrown and the transaction is rolled back.
     */
    Upload finalizeAttempt(UUID attemptId, String uploadKey, @Nullable Collection<RegisterVariantArgs> variants, UUID originalAssetId);
}
