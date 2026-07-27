package com.example.image_transformer;

import com.example.image_transformer.operations.ImageTransformationService;
import com.example.image_transformer.task_service.TaskService;
import com.example.transformer_contracts.webhook.WebhookService;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = {
        ImageTransformationService.class,
        TaskService.class,
        WebhookService.class
})
public class TestApplication {
}
