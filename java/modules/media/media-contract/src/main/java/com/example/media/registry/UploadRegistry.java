package com.example.media.registry;

import com.example.media.identity.AttemptId;
import com.example.media.identity.UploadKey;
import com.example.media.upload.Upload;
import com.example.media.upload.UploadProcess;
import com.example.media.variant.Variant;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * Upload lifecycle storage. The caller owns the transaction around {@link #publish}.
 * This port does not open one and does not accept transformation bodies.
 */
@NullMarked
public interface UploadRegistry {
    /** Create the process when the key is new, then lock it. */
    UploadProcess lockOrCreate(UploadKey uploadKey);

    /**
     * Insert an attempt and make it the active attempt.
     * Throws when the process is already published.
     */
    AttemptId startAttempt(NewAttempt attempt) throws UploadAlreadyPublished;

    /** Move the attempt to processing. Idempotent when it is already processing. */
    void markProcessing(AttemptId attemptId);

    /** Mark the attempt failed after retries are exhausted or the error is unrecoverable. Idempotent. */
    void markFailed(AttemptId attemptId);

    /**
     * Publish the attempt when it is still active: insert the upload and variants, mark those assets ready,
     * and mark the attempt successful.
     * A repeated call for the attempt that already published this upload returns that upload.
     * A different attempt for a published key throws {@link UploadAlreadyPublished}.
     */
    Upload publish(Publication publication) throws UploadAlreadyPublished, AttemptNotActive;

    Optional<Upload> findUpload(UploadKey uploadKey);

    Optional<Variant> findVariant(UploadKey uploadKey, String transformationName);
}
