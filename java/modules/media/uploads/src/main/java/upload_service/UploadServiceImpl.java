package upload_service;

import com.example.repository.ObjectStorageRepository;
import com.example.uploads.UploadId;
import upload_repository.UploadRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@RequiredArgsConstructor
public class UploadServiceImpl {
    private final UploadRepository uploadRepository;
    private final ObjectStorageRepository objectStorageRepository;

    @NotNull Map<String, String> createSignedUpload(){
        return objectStorageRepository.getPreSignedUploadUrl("uploads", "test.txt", 60);
    }

    boolean markUploadAsComplete(@NotNull UploadId uploadId){
        var upload = uploadRepository.getById(uploadId);
    }
}
