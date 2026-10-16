package com.example.upload_registry.upload;

import com.example.uploads_api.uploads.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@NullMarked
@Repository
@RequiredArgsConstructor
public class UploadRepositoryImpl implements UploadRepository {
    private final UploadCrudRepository uploadCrudRepository;

    @Override
    public Upload insert(InsertUpload upload) throws UploadKeyTakenException {
        return null;
    }

    @Override
    public void updateStatus(UUID uploadId, UploadStatus status) {

    }

    @Override
    public Upload findByKey(String uploadKey) {
        return null;
    }

    @Override
    public Upload findImageByKey(String uploadKey) {
        return null;
    }

    @Override
    public void deleteById(String uploadKey) {

    }
}
