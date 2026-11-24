package com.example.media.api.upload;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.upload.AttemptOutcome;
import com.example.media.contract.upload.UploadAttempt;
import com.example.media.contract.upload.UploadAttemptStatus;
import com.example.media.contract.upload.UploadKey;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FailImageUploadTest {

    private final UploadRegistry uploadRegistry = mock();
    private final FailImageUpload useCase = new FailImageUpload(
            uploadRegistry, new TransactionTemplate(mock(PlatformTransactionManager.class)));

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final UploadKey KEY = new UploadKey("cover");

    @Test
    void marksAPendingAttemptFailed() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(attempt(UploadAttemptStatus.PENDING)));

        useCase.fail(ATTEMPT_ID);

        verify(uploadRegistry).finishAttempt(ATTEMPT_ID, AttemptOutcome.FAILED);
    }

    @Test
    void leavesASuccessfulAttemptAlone() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(attempt(UploadAttemptStatus.SUCCESS)));

        useCase.fail(ATTEMPT_ID);

        verify(uploadRegistry, never()).finishAttempt(any(), any());
    }

    @Test
    void leavesAFailedAttemptAlone() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(attempt(UploadAttemptStatus.FAILED)));

        useCase.fail(ATTEMPT_ID);

        verify(uploadRegistry, never()).finishAttempt(any(), any());
    }

    @Test
    void ignoresAnUnknownAttempt() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.empty());

        useCase.fail(ATTEMPT_ID);

        verify(uploadRegistry, never()).finishAttempt(any(), any());
    }

    private static UploadAttempt attempt(UploadAttemptStatus status) {
        return new UploadAttempt(
                ATTEMPT_ID,
                KEY,
                AssetId.generate(),
                List.of(),
                null,
                status,
                Instant.now(),
                status == UploadAttemptStatus.PENDING ? null : Instant.now());
    }
}
