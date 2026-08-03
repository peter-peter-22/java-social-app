package com.example.uploads_persistence;

import com.example.uploads_persistence.utils.TestUploadPersistenceConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TestUploadPersistenceConfiguration.class)
public class TestApplication {
}
