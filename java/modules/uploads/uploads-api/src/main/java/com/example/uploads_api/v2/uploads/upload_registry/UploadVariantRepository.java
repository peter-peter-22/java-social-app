package com.example.uploads_api.v2.uploads.upload_registry;

import com.example.uploads_api.uploads.UploadId;

import java.util.Collection;

public interface UploadVariantRepository {
    UploadVariant insert(InsertUploadVariant insert);

    UploadVariant insertAll(Collection<InsertUploadVariant> insert);

    UploadVariant getById(UploadId id);
}
