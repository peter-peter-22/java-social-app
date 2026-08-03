package com.example.uploads_api.v2.uploads.upload_object;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;

public interface UploadObjectRepository {
    // TODO should this be deleted?
    void downloadOriginal(String uploadKey, String homeRegion, Path destination);

    void uploadOriginal(String uploadKey, Path source);

    void uploadVariants(Collection<UploadVariantObjectArgs> objects);

    InputStream getVariant(String uploadKey, String variantName, String relativePath, String homeRegion);
}
