package upload_repository;

import com.example.uploads.MediaType;
import com.example.uploads.UploadStatus;
import com.example.users.repository.UserId;
import org.jetbrains.annotations.NotNull;

public record InsertUpload(
        @NotNull UserId createdBy,
        @NotNull String bucketName,
        @NotNull MediaType mediaType,
        @NotNull UploadStatus status,
        @NotNull String fileExtension
) {
}
