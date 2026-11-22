package com.example.media.imageworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The image worker deployable: HTTP in, libvips and object storage out. No database.
 * The Minio object store lives outside this package in {@code com.example.object_storage}, so it is
 * scanned in; the worker keeps no registry, no Liquibase, and no shared service module.
 */
@SpringBootApplication(scanBasePackages = {"com.example.media.imageworker", "com.example.object_storage"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
