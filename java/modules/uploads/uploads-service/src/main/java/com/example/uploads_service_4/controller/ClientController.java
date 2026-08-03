package com.example.uploads_service_4.controller;

import com.example.object_storage.repository.ObjectStorageRepository;
import com.example.uploads_service_4.upload_service_2.SignedUploadMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {
    private ObjectStorageRepository objectStorageRepository;

    @PostMapping("/upload")
    void upload(@RequestPart("file") MultipartFile file, @RequestPart("metadata") @Validated SignedUploadMetadata metadata) {

    }
}
