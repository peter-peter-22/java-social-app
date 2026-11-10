package com.example.uploads_api.uploads.upload_process;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public interface UploadProcessRepository {
    /** Create a new upload process for this upload key if it does not exist, do nothing otherwise.
     * Return the upload process in either case.
     * Use pessimistic lock the upload process. */
    UploadProcess createProcessIfNotExistsThenLock(String uploadKey);
    /** Insert a new upload attempt. */
    UUID createAttempt(InsertUploadAttempt insert);
    /** Get an upload attempt by id. Use pessimistic locking. */
    @Nullable UploadAttempt getAndLockAttempt(UUID attemptId);
    /** Set an upload attempt as active in an upload process.
     * Has effect only when the upload process has no upload ID set. (So when the upload is not finished.)
     * Throws error when the process is finished. */
    // TODO should throw UploadAlreadyExistsException("Upload with uploadKey %s is already finished, cannot start a new attempt.".formatted(uploadKey));
    void setAttemptAsActive(String uploadKey, UUID attemptId) throws UploadAlreadyExistsException;
    /** Add a new asset to the created assets of an attempt. */
    void registerCreatedAsset(UUID attemptId, UUID assetId);
    void updateStatus(UUID attemptId, UploadAttemptStatus status);
}
