package upload_repository;

import com.example.uploads.MediaType;
import com.example.uploads.UploadStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("uploads")
record UploadEntity(
        @Id UUID id,
        UUID createdBy,
        String bucketName,
        MediaType mediaType,
        Instant createdAt,
        UploadStatus status
        // region
) {
}
