package com.example.transformer_contracts.storage;

import java.nio.file.Path;

public class TestResourcesDirectory {
    public static Path getResourcesPath() {
        return Path.of("src/test/resources");
    }
}
