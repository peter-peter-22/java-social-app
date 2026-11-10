package com.example.object_storage.reconciliation.changelist.buckets;

import com.example.object_storage.reconciliation.MinioReconciliation;
import com.example.object_storage.regional.MinioProperties;
import com.example.object_storage.tags.ObjectTag;
import io.minio.MinioClient;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
class Uploads extends MinioReconciliation {
    private final MinioProperties minioProperties;

    @Override
    public String getName() {
        return "Create the uploads bucket";
    }

    @Override
    public void apply(MinioClient client) {
        String bucketName = minioProperties.uploadsConfiguration().bucketName();
        int expirationDays = minioProperties.uploadsConfiguration().expirationDays();

        // The bucket is private by default
        createBucketIfNotExists(client, bucketName);

        // Configure the tag that expires files
        var expirationTag= ObjectTag.EXPIRES;
        List<LifecycleRule> rules = new ArrayList<>();
        rules.add(
                new LifecycleRule(
                        Status.ENABLED,
                        new AbortIncompleteMultipartUpload(expirationDays),
                        new Expiration((ZonedDateTime) null, expirationDays, null), // TODO check the ideal parameters
                        new RuleFilter(new Tag(expirationTag.getKey(), expirationTag.getValue())),
                        "expiration tag rule",
                        null,
                        null,
                        null
                )
        );
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        try {
            client.setBucketLifecycle(SetBucketLifecycleArgs.builder().bucket(bucketName).config(config).build());
        } catch (Exception e) {
            throw  new RuntimeException("Failed to set bucket lifecycle", e);
        }
    }
}
