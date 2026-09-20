package com.example.object_storage.reconciliation.changelist.buckets;

import com.example.object_storage.reconciliation.MinioReconciliation;
import io.minio.MinioClient;
import org.springframework.stereotype.Component;

@Component
class Private extends MinioReconciliation {

    @Override
    public String getName() {
        return "Create private bucket";
    }

    @Override
    public void apply(MinioClient client) {
        String bucketName = "private";

        // The bucket is private by default
        createBucketIfNotExists(client, bucketName);
    }
}
