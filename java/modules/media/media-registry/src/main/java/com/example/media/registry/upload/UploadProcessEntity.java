package com.example.media.registry.upload;

import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.upload.UploadId;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadProcess;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

/**
 * The {@code upload_process} table: the coordination record of one upload key.
 * The upload key serves as the primary key.
 */
@Entity
@Table(name = "upload_process")
@Getter
@NoArgsConstructor
@SuperBuilder
public final class UploadProcessEntity {
    @Id
    private String uploadKey;
    /**
     * The attempt currently being processed. Nullable so a process can be inserted before its first
     * attempt, which breaks the circular foreign key with {@link UploadAttemptEntity}.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_attempt_id")
    @Setter
    private @Nullable UploadAttemptEntity activeAttempt;
    /**
     * Set when the upload finished successfully.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id")
    @Setter
    private @Nullable UploadEntity upload;

    public UploadProcess toModel() {
        return new UploadProcess(
                new UploadKey(uploadKey),
                activeAttempt == null ? null : new AttemptId(activeAttempt.getAttemptId()),
                upload == null ? null : new UploadId(upload.getId())
        );
    }
}
