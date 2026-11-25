package com.example.media.api.upload;

import com.example.media.contract.port.UploadPermissionRegistry;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.upload.AttemptOutcome;
import com.example.media.contract.upload.UploadAttemptStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Marks an attempt failed when the worker gave up: the retries were exhausted or the failure
 * was unrecoverable. Idempotent: an attempt that already finished, or that no longer exists, is left
 * alone, so a duplicate dead-lettered command and a racing completion cannot overwrite a terminal
 * state that matters.
 *
 * <p>A failed attempt also revokes what is left of the permission it consumed, in the same
 * transaction. The use the attempt spent stays spent, so a retry needs a new permission. A
 * successful attempt leaves its permission alone (see {@link CompleteImageUpload}).</p>
 */
@Service
@RequiredArgsConstructor
public class FailImageUpload {
    private final UploadRegistry uploadRegistry;
    private final UploadPermissionRegistry uploadPermissionRegistry;
    private final TransactionTemplate transactions;

    public void fail(AttemptId attemptId) {
        transactions.execute(status -> {
            var attempt = uploadRegistry.findAttempt(attemptId).orElse(null);
            if (attempt == null
                    || attempt.status() == UploadAttemptStatus.SUCCESS
                    || attempt.status() == UploadAttemptStatus.FAILED)
                return null;
            uploadRegistry.finishAttempt(attemptId, AttemptOutcome.FAILED);
            // Revoking inside the same transaction keeps the terminal status and the revoked
            // budget together: a permission either loses its uses with the failure or keeps them.
            uploadPermissionRegistry.revokeForAttempt(attemptId);
            return null;
        });
    }
}
