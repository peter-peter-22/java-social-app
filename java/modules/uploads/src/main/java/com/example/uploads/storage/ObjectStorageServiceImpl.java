package com.example.uploads.storage;

import com.example.uploads.upload_repository.UploadRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ObjectStorageServiceImpl {
    @NotNull private final UploadRepository uploadRepository;


}
