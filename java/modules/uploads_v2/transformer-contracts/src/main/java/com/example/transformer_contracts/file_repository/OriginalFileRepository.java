package com.example.transformer_contracts.file_repository;

import com.example.object_storage.repository.ObjectStorageRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

@Repository
class OriginalFileRepository extends FileBucketRepository {

    public OriginalFileRepository(ObjectStorageRepository objectStorageRepository) {
        super(objectStorageRepository);
    }

    @Override
    protected @NonNull String bucketName() {
        return "originals";
    }
}
