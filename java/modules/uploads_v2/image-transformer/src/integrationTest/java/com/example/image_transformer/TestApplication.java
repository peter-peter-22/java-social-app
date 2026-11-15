package com.example.image_transformer;

import com.example.image_transformer.task_service.TaskService;
import com.example.image_transformer.transformation.ImageTransformationService;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = {
        ImageTransformationService.class,
        TaskService.class,
})
public class TestApplication {
}
