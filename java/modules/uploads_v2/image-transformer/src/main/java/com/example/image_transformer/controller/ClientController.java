package com.example.image_transformer.controller;

import com.example.image_transformer.task_service.TaskService;
import com.example.uploads_api.dto.ImageProcessingTask;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
public class ClientController {
    private final TaskService taskService;

    @PostMapping()
    void process(@RequestBody ImageProcessingTask task) {
        taskService.processTask(task);
    }
}
