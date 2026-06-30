package com.example.configuration.reconciliations.buckets;

import com.example.configuration.MinioConfiguration;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.springframework.stereotype.Component;

@Component
class Public extends MinioConfiguration {

    public Public(MinioClient minioClient) {
        super(minioClient);
    }

    @Override
    public String getName() {
        return "Create public bucket";
    }

    @Override
    public void apply() throws Exception {
        String bucketName = "public";

        createBucketIfNotExists(bucketName);

        var args = SetBucketPolicyArgs.builder().bucket(bucketName).config("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                              "Sid": "PublicRead",
                              "Effect": "Allow",
                              "Principal": { "AWS": ["*"] },
                              "Action": [
                                    "s3:GetObject",
                                    "s3:GetObjectVersion"
                              ],
                              "Resource": ["arn:aws:s3:::public/*"]
                        }
                    ]
                }
                """).build();
        minioClient.setBucketPolicy(args);
    }
}
