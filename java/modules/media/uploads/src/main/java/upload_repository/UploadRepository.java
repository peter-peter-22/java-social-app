package upload_repository;

import com.example.uploads.Upload;
import com.example.uploads.UploadId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UploadRepository {
    @Nullable Upload getById(@NotNull UploadId id);
    @NotNull UploadId create(@NotNull InsertUpload upload) throws UploadMissingUserException;
    void update(@NotNull Upload save);
}
