package com.example.image_transformer;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.example.image_transformer.webhook",
        "com.example.image_transformer.operations",
        "com.example.image_transformer.task_service"
})
public class TestApplication {
}
