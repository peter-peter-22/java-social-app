package com.example.uploads_persistence.utils;

import com.example.users_persistence.utils.TestUserPersistence;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@ComponentScan(basePackages = {
        "com.example.uploads_persistence.upload_repository",
        "com.example.users_persistence.repository"
})
@Import({TestUserPersistence.class, TestUploadPersistence.class})
public class TestUploadPersistenceConfiguration {
}
