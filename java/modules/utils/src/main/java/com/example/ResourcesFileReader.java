package com.example;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.IOException;

public class ResourcesFileReader {
    public String readString(String path) throws IOException {
        Resource resource = new ClassPathResource(path); // File in src/main/resources
        return resource.getFile().toString();
    }
}   