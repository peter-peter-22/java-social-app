package com.example.upload_registry.upload_process;

import com.example.upload_registry.upload.UploadEntity;
import com.example.uploads_api.uploads.upload_process.UploadProcess;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "upload_process")
@Getter
@NoArgsConstructor
@SuperBuilder
public final class UploadProcessEntity {
    /** The upload key serves as the unique ID of the upload process. */
    @Id
    private String uploadKey;
    /**
     * The attempt currently being processed. Nullable so a process can be inserted before its first attempt,
     * which breaks the circular foreign key with {@link UploadAttemptEntity}.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_attempt_id")
    @Setter
    private @Nullable UploadAttemptEntity activeAttempt;
    /** Set when the upload finished successfully. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id")
    @Setter
    private @Nullable UploadEntity upload;

    public UploadProcess toModel() {
        return UploadProcess.builder()
                .uploadKey(uploadKey)
                .activeAttemptId(activeAttempt == null ? null : activeAttempt.getAttemptId())
                .uploadId(upload == null ? null : upload.getId())
                .build();
    }
}
