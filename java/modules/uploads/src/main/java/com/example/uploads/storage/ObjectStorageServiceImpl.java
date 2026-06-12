package com.example.uploads.storage;

import com.example.uploads.registry.upload.UploadRepository;
import com.example.uploads.registry.upload_variant.UploadVariantRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ObjectStorageServiceImpl {
    @NotNull private final UploadRepository uploadRepository;
    @NotNull private final UploadVariantRepository uploadVariantRepository;

}
