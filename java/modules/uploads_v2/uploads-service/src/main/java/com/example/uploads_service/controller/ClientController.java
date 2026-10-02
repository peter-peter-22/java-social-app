package com.example.uploads_service.controller;

import com.example.uploads_service.api.dto.ImageUploadRequest;
import com.example.uploads_service.api.uploads.FileType;
import com.example.uploads_service.api.uploads.UploadType;
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
    @PostMapping("/upload")
    void upload(@RequestPart("file") MultipartFile file, @RequestPart("metadata") @Validated ImageUploadRequest uploadRequest) {

        // insert upload entity
        var fileType = FileType.getByContentType(uploadRequest.getContentType());
        var uploadType = UploadType.IMAGE;
        var extension = fileType == null ? null : fileType.getExtension();

        // TODO process error
        // check if the constraints pass
        if (uploadRequest.getBytes()!=null && file.getSize() != uploadRequest.getBytes())
            throw new RuntimeException("The file size does not match the excepted value.");
        if (uploadRequest.getContentType()!=null && !uploadRequest.getContentType().equals(file.getContentType()))
            throw new RuntimeException("The file content type does not match the excepted value.");


    }
}
