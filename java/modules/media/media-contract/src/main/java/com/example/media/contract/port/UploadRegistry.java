package com.example.media.contract.port;

import com.example.media.contract.upload.*;
import com.example.media.contract.variant.NewVariant;
import com.example.media.contract.variant.Variant;

import java.util.Collection;
import java.util.Optional;

/**
 * The upload lifecycle rows: processes, attempts, uploads, and variants.
 *
 * <p>Each method is a single operation that joins the caller's transaction. The finalize
 * transaction boundary belongs to the application service, not to this port: the use case orders
 * the asset-status updates, the object-store calls, and these writes into one commit. Methods
 * documented as atomic are safe to rely on even outside a wrapping transaction.</p>
 */
public interface UploadRegistry {

    /**
     * Returns the process of the key, creating it if absent, and locks it for the rest of the
     * caller's transaction. Atomic on its own.
     */
    UploadProcess openOrGetProcess(UploadKey key);

    /** Registers a new attempt in status {@code PENDING} and returns its id. */
    AttemptId createAttempt(NewUploadAttempt insert);

    /**
     * Makes the attempt the active attempt of its process.
     * Atomic on its own.
     *
     * @throws UploadAlreadyFinishedException when the process already has its final upload.
     */
    void activateAttempt(UploadKey key, AttemptId attemptId) throws UploadAlreadyFinishedException;

    Optional<UploadAttempt> findAttempt(AttemptId id);

    /** Closes the attempt with its terminal status and finish date. */
    void finishAttempt(AttemptId id, AttemptOutcome outcome);

    /**
     * Registers the final upload of an attempt and points the process at it.
     * Atomic on its own: the upload key must be free and the attempt must still be the active one
     * of its unfinished process, otherwise nothing is written.
     *
     * @throws UploadKeyTakenException when the key is already taken by another upload.
     * @throws AttemptNotActiveException when the attempt is not the active attempt of the process,
     *                                   or the process already has its upload.
     */
    Upload registerUpload(NewUpload insert) throws UploadKeyTakenException, AttemptNotActiveException;

    /** Registers variant rows. Inserts only; a duplicate variant is an error and writes nothing. */
    void registerVariants(Collection<NewVariant> variants);

    Optional<Upload> findUpload(UploadKey key);

    Optional<Variant> findVariant(UploadKey key, String transformationName);
}