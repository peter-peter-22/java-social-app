package com.example.media.api.upload;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.port.UploadPermissionRegistry;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.upload.AttemptOutcome;
import com.example.media.contract.upload.UploadAttempt;
import com.example.media.contract.upload.UploadAttemptStatus;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadVisibility;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FailImageUploadTest {

    private final UploadRegistry uploadRegistry = mock();
    private final UploadPermissionRegistry uploadPermissionRegistry = mock();
    private final FailImageUpload useCase = new FailImageUpload(
            uploadRegistry, uploadPermissionRegistry,
            new TransactionTemplate(mock(PlatformTransactionManager.class)));

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final UploadKey KEY = new UploadKey("cover");

    @Test
    void marksAPendingAttemptFailed() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(attempt(UploadAttemptStatus.PENDING)));

        useCase.fail(ATTEMPT_ID);

        verify(uploadRegistry).finishAttempt(ATTEMPT_ID, AttemptOutcome.FAILED);
        // The remaining uses of the permission the attempt consumed are revoked with the failure.
        verify(uploadPermissionRegistry).revokeForAttempt(ATTEMPT_ID);
    }

    @Test
    void leavesASuccessfulAttemptAlone() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(attempt(UploadAttemptStatus.SUCCESS)));

        useCase.fail(ATTEMPT_ID);

        verify(uploadRegistry, never()).finishAttempt(any(), any());
        verifyNoInteractions(uploadPermissionRegistry);
    }

    @Test
    void leavesAFailedAttemptAlone() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.of(attempt(UploadAttemptStatus.FAILED)));

        useCase.fail(ATTEMPT_ID);

        verify(uploadRegistry, never()).finishAttempt(any(), any());
        verifyNoInteractions(uploadPermissionRegistry);
    }

    @Test
    void ignoresAnUnknownAttempt() {
        when(uploadRegistry.findAttempt(ATTEMPT_ID)).thenReturn(Optional.empty());

        useCase.fail(ATTEMPT_ID);

        verify(uploadRegistry, never()).finishAttempt(any(), any());
        verifyNoInteractions(uploadPermissionRegistry);
    }

    private static UploadAttempt attempt(UploadAttemptStatus status) {
        return new UploadAttempt(
                ATTEMPT_ID,
                KEY,
                AssetId.generate(),
                List.of(),
                null,
                UploadVisibility.PUBLIC,
                status,
                Instant.now(),
                status == UploadAttemptStatus.PENDING ? null : Instant.now());
    }
}
