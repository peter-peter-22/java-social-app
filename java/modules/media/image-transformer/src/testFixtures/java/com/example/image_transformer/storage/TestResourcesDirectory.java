package com.example.image_transformer.storage;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestResourcesDirectory {
    public static Path getResourcesPath() {

        var testRelative = Path.of("src/test/resources");
        if (Files.isDirectory(testRelative)) {
            return testRelative;
        }

        var moduleIntegrationRelative = Path.of("modules/media/image-transformer/src/integrationTest/resources");
        if (Files.isDirectory(moduleIntegrationRelative)) {
            return moduleIntegrationRelative;
        }

        return Path.of("modules/media/image-transformer/src/test/resources");
    }
}
