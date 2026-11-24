package com.example.media.api.upload;

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
 */
@Service
@RequiredArgsConstructor
public class FailImageUpload {
    private final UploadRegistry uploadRegistry;
    private final TransactionTemplate transactions;

    public void fail(AttemptId attemptId) {
        transactions.execute(status -> {
            var attempt = uploadRegistry.findAttempt(attemptId).orElse(null);
            if (attempt == null
                    || attempt.status() == UploadAttemptStatus.SUCCESS
                    || attempt.status() == UploadAttemptStatus.FAILED)
                return null;
            // TODO This failed attempt state might be totally unnecessary.
            //  Investigate this after the periodic cleanup is complete and the most of the media cloud works.
            uploadRegistry.finishAttempt(attemptId, AttemptOutcome.FAILED);
            return null;
        });
    }
}
