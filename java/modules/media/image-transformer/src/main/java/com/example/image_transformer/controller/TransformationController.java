package com.example.image_transformer.controller;

import com.example.image_transformer.task.ImageTransformationTaskMapper;
import com.example.image_transformer.transformation.TaskService;
import com.example.media_api.transformations.dto.ImageTransformationTaskGroupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transform")
@RequiredArgsConstructor
public class TransformationController {
    private final TaskService transformationService;

    @PostMapping()
    void markAsReady(@RequestBody ImageTransformationTaskGroupDTO body) {
        var tasks = ImageTransformationTaskMapper.createFromGroupedDTO(body);
        transformationService.processTasks(tasks);
        // TODO: add webmvc test
    }
}
