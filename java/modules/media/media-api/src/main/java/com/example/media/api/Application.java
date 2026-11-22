package com.example.media.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The media API deployable: HTTP in, the registry and object storage out.
 * The driven adapters are wired by {@link MediaPersistenceConfiguration}. Keeping their scanning and
 * JPA enabling there, rather than on this entry point, keeps web-slice tests (which do not
 * auto-configure JPA) from trying to enable the repositories.
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
