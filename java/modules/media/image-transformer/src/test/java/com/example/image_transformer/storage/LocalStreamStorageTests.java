package com.example.image_transformer.storage;

import com.example.media_api.uploads.ObjectLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LocalStreamStorageTests {
    private static final ObjectLocation TEST_FILE_LOCATION = new ObjectLocation("test.txt", "test-files");
    private static final ObjectLocation OUTPUT_FILE_LOCATION = new ObjectLocation("copied.txt", "test-files");
    private static final LocalStreamStorage STORAGE = new LocalStreamStorage();

    @Test
    void copyTest() throws IOException {
        try (var stream = STORAGE.read(TEST_FILE_LOCATION)) {
            STORAGE.write(stream, OUTPUT_FILE_LOCATION);
        }
    }
}
