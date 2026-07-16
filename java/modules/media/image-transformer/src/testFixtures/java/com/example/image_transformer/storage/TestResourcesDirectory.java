package com.example.image_transformer.storage;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestResourcesDirectory {
    public static Path getResourcesPath() {
        return Path.of("src/test/resources");
    }
}
